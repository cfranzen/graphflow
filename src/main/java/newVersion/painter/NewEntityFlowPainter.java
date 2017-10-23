package newVersion.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gui.MyMap;
import main.RouteController;
import models.Constants;
import models.Edge;
import models.SeaEdge;
import newVersion.models.FlowEntity;
import newVersion.models.MapNode;
import newVersion.models.NodeEdge;
import newVersion.models.SeaFlowEntity;
import painter.DefaultRoutePainter;
import painter.IRoutePainter;
import painter.SeaRoutePainter;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class NewEntityFlowPainter implements IRoutePainter {

	private static final Logger logger = LoggerFactory.getLogger(NewEntityFlowPainter.class);
	private static int timeStep = 0;
	private RouteController routeController;
	private Color lastCol = Color.GRAY;
	private List<FlowEntity> entities = new ArrayList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.IRoutePainter#setTimeStep(int)
	 */
	@Override
	public void setTimeStep(int time) {
		timeStep = time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#setRouteController(main.RouteController)
	 */
	@Override
	public void setRouteController(RouteController routeController) {
		this.routeController = routeController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#drawRoute(java.awt.Graphics2D, gui.MyMap)
	 */
	@Override
	public void drawRoute(Graphics2D g, MyMap map) {

		calcOnlyVisibleEdges(g, map);

		List<NodeEdge> seaRoute = new ArrayList<>();
		for (Edge edge : routeController.getSeaRoute()) {
			seaRoute.add((NodeEdge) edge);
		}
		List<SeaEdge> seaEdges = SeaRoutePainter.calcDrawEdges(seaRoute);
		for (NodeEdge nodeEdge : seaRoute) {
			nodeEdge.setPath(new ArrayList<>(), 0);
			nodeEdge.setPathZoom(map.getZoom());
			for (SeaEdge seaEdge : seaEdges) {
				if (seaEdge.edgeIds.contains(nodeEdge.id)) {
					nodeEdge.addToPath(seaEdge.getPath());
				}
			}
		}
		// Manipulate the graphics object one time before the loop instead of
		// every run, because it is a resource consuming operation
		g.setColor(Color.GRAY);
		g.setStroke(new BasicStroke(1.2f));
		for (int i = 0; i < routeController.getSeaRoute().size(); i++) {
			NodeEdge edge = (NodeEdge) routeController.getSeaRoute().get(i);
			drawGreyEdgeLine(g, map, edge);
		}
		if (Constants.optimzeLandRoutes) {
			for (Edge edge : routeController.getPaintRoute()) {
				drawGreyEdgeLine(g, map, (NodeEdge) edge);
			}
		}

		// Every bigger time step new entities are starting from the nodes
		int timeStepBig = timeStep / Constants.PAINT_STEPS;
		if (timeStep % Constants.PAINT_STEPS == 0) {

			// Create land entities
			if (Constants.optimzeLandRoutes) {
				List<Edge> route = routeController.getPaintRoute();
				for (int i = 0; i < route.size(); i++) {

					NodeEdge edge = (NodeEdge) route.get(i);
					int serviceTime = (int) edge.getServiceTime(timeStepBig);
					FlowEntity e = new FlowEntity(serviceTime, timeStepBig, edge);
					entities.add(e);
				}
			}

			// Create sea entities
			for (int i = 0; i < routeController.getSeaRoute().size(); i++) {
				NodeEdge edge = (NodeEdge) routeController.getSeaRoute().get(i);
				int serviceTime = (int) edge.getServiceTime(timeStepBig);
				if (serviceTime > 0) {
					if (Constants.debugInfosSeaEdges) {
						logger.info(edge.id + ": ServiceTime: " + serviceTime);
					}
					SeaFlowEntity e = new SeaFlowEntity(serviceTime, timeStepBig, edge);
					entities.add(e);
				}
			}

			// Increase the TTL of the entities
			List<FlowEntity> newEntityList = new ArrayList<>();
			for (FlowEntity flowEntity : entities) {
				if (flowEntity.next()) {
					newEntityList.add(flowEntity);
				}
			}
			entities = newEntityList;
		}
		if (Constants.debugInfosSeaEdges) {
			SeaRoutePainter.drawPossibleSeaEdges(g);
		}
		for (FlowEntity flowEntity : entities) {
			if (Constants.debugInfos) {
				g.setColor(Color.BLUE);
				g.setStroke(new BasicStroke(2f));
				double pSize = flowEntity.edge.getPoints().size() - 1;
				double ppStep = (pSize) / flowEntity.getMaxServiceTimeSteps();

				for (int i = 0; i < pSize - 10; i += ppStep) {

					GeoPosition geoPos = flowEntity.getPoints().get(i).getPosition();
					Point2D point = map.getTileFactory().geoToPixel(geoPos, map.getZoom());
					g.drawOval((int) point.getX() - 5, (int) point.getY() - 5, 10, 10);
				}

			}

			Path2D path = flowEntity.getPath();
			if (path == null) {
				drawLineEntity(g, map, timeStepBig, flowEntity);
			} else {
				MapNode currentNode = flowEntity.getPoints().get(flowEntity.capWorkIndex);
				long capacity = currentNode.capWork[flowEntity.startTimestep][0];
				long workload = currentNode.capWork[flowEntity.startTimestep][1];
				modifyGraphics(g, capacity, workload, true);
				g.draw(path);
			}
		}
	}

	/**
	 * Draws a directed line for the given entity with a fixed length which is
	 * in relation to the point count of the edge.
	 * 
	 * @param g
	 *            the graphics component, needed for drawing lines
	 * @param map
	 *            the map on which the lines should be drawn, needed for
	 *            conversion from {@link GeoPosition} to Pixel
	 * @param timeStepBig
	 *            identifies the index of the nodes information for capacity and
	 *            workload
	 * @param entity
	 *            the {@link FlowEntity} which should be drawn
	 */
	private void drawLineEntity(Graphics2D g, MyMap map, int timeStepBig, FlowEntity entity) {
		int[] minMax = calcMinMaxIndex(entity);
		// Edge should contain min. 2 Positions (Start + End)
		GeoPosition last = entity.getPoints().get(minMax[0]).getPosition();
		for (int i = minMax[0]; i < minMax[1] && i < entity.getPoints().size(); i++) {
			MapNode node = entity.getPoints().get(i);
			GeoPosition current = node.getPosition();

			drawRoutePart(g, map, last, current, node.capWork[timeStepBig][0], node.capWork[timeStepBig][1]);

			last = current;
		}

	}

	public static int[] calcMinMaxIndex(FlowEntity entity) {
		final int LENGTH = 8; // TODO Change: Length of the entity line in relation to the
		// edge point count
		double pointsPerTimeStep = (double) (entity.edge.getPathSize()) / entity.getMaxServiceTimeSteps()
				/ Constants.PAINT_STEPS;
		int max = (int) Math.ceil((pointsPerTimeStep * (timeStep % Constants.PAINT_STEPS)
				+ ((entity.getCurrentServiceTimeStep() - 1) * (pointsPerTimeStep * Constants.PAINT_STEPS))));
		int min = (int) Math.floor(max - pointsPerTimeStep * LENGTH);

		min = (int) ((min >= entity.edge.getPathSize()) ? entity.edge.getPathSize() - 1 : (min < 0) ? 0 : min);
		max = (int) ((max >= entity.edge.getPathSize()) ? entity.edge.getPathSize() - 1 : max);
		
		return new int[] { min, max };
	}

	private void drawGreyEdgeLine(Graphics2D g, MyMap map, NodeEdge edge) {
		// Grapic maniuplation has to be done before the loop
		// g.setColor(Color.DARK_GRAY);
		// g.setStroke(new BasicStroke(1.2f));
		g.draw(edge.getShape(map));

	}

	private void drawRoutePart(Graphics2D g, MyMap map, GeoPosition from, GeoPosition to, long capacity,
			long workload) {
		modifyGraphics(g, capacity, workload);
		DefaultRoutePainter.drawNormalLine(g, map, from, to);
	}
	
	
	private void modifyGraphics(Graphics2D g, long capacity, long workload) {
		modifyGraphics(g, capacity, workload, false);
	}

	private void modifyGraphics(Graphics2D g, long capacity, long workload, boolean isShip) {
		// modifying the graphics component is work intensive,
		Color curCol = DefaultRoutePainter.calculateColor(workload, capacity);
		if (!curCol.equals(lastCol)) {
			
			g.setColor(curCol);
			float scale = capacity / 250f;
			if (isShip) {
				scale /= Constants.SHIP_SCALE_FACTOR;
			}
			g.setStroke(new BasicStroke(scale));
		}
	}

	private void calcOnlyVisibleEdges(Graphics2D g, MyMap map) {
		Rectangle rect = map.getViewportBounds();
		GeoPosition viewportStart = map.getGeoPos(rect.getMinX(), rect.getMinY());
		GeoPosition viewportEnd = map.getGeoPos(rect.getMaxX(), rect.getMaxY());
		routeController.excludeNonVisiblePointFromPaintRoutes(viewportStart, viewportEnd);
		if (Constants.debugInfos && Constants.drawOnlyViewport) {
			g.setColor(Color.MAGENTA);
			g.drawRect((int) rect.getMinX() + 300, (int) rect.getMinY() + 150,
					(int) rect.getMaxX() - 300 - ((int) rect.getMinX() + 300),
					(int) rect.getMaxY() - 150 - ((int) rect.getMinY() + 150));
		}
	}

}
