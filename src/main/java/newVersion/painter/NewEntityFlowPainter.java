package newVersion.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

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

	private int timeStep = 0;
	private RouteController routeController;

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

		for (Edge edge : routeController.getPaintRoute()) {
			if (Constants.optimzeLandRoutes) {
				drawGreyEdgeLine(g, map, (NodeEdge) edge);
			}
		}
		for (Edge edge : routeController.getPaintSeaRoute()) {
//			drawGreyEdgeLine(g, map, (NodeEdge) edge);
		} 

		int timeStepBig = timeStep / Constants.PAINT_STEPS;

		// Every bigger time step new Entities are starting from the nodes
		if (timeStep % Constants.PAINT_STEPS == 0) {
			// Create land entities
			if (Constants.optimzeLandRoutes) {
				List<Edge> route = routeController.getPaintRoute();
				for (int i = 0; i < route.size(); i++) {
	
					NodeEdge edge = (NodeEdge)route.get(i);
					int serviceTime = (int) edge.getServiceTime(timeStepBig);
	
					FlowEntity e = new FlowEntity(serviceTime, timeStepBig, edge);
					entities.add(e);
				}
			}
			// Create sea entities
			List<NodeEdge> route = new ArrayList<>();
			for (Edge nodeEdge :   routeController.getSeaRoute()) {
				route.add((NodeEdge) nodeEdge);
			}
			
			
			// TODO Call lazy if no path element for the current zoom level is present
			List<SeaEdge> seaPaths = SeaRoutePainter.calcDrawEdges(route);
			
			for (SeaEdge seaEdge : seaPaths) {
				NodeEdge edge = new NodeEdge(seaEdge);
				for (int i = 0; i < Constants.MAX_ZOOM_LEVEL; i++) {
					// FIXME Calc for every zoom level
					edge.path[i] = (Double) seaEdge.getPath();
				} 
			
				drawGreyEdgeLine(g, map, (NodeEdge) edge);
			} 
			
			
			for (int i = 0; i < route.size(); i++) {
				NodeEdge edge = route.get(i);
				int serviceTime = (int) edge.getServiceTime(timeStepBig);
				
				List<Path2D> path = new ArrayList<>();
				for (SeaEdge seaEdge : seaPaths) {
//					if (seaEdge.edgeIds.contains(edge.id)) {
						path.add(seaEdge.getPath());
//					}
				}
				
				SeaFlowEntity e = new SeaFlowEntity(serviceTime, timeStepBig, edge);
				// TODO Add Points before adding entity
				e.setPath(path);
				entities.add(e);
				
			}
			

			List<FlowEntity> newEntityList = new ArrayList<>();
			for (FlowEntity flowEntity : entities) {
				if (flowEntity.next()) {
					newEntityList.add(flowEntity);
				} 
			}
			entities = newEntityList;
		}
		for (FlowEntity flowEntity : entities) {
			Path2D.Double path = flowEntity.getPath();
			if (path == null) {
				drawLineEntity(g, map, timeStepBig, flowEntity);
			} else {
				// modifying the graphics component is work intensive,
				g.setColor(Color.BLUE);
				g.setStroke(new BasicStroke(50));
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
		final int LENGTH = 8; // Length of the entity line in relation to the
								// edge point count

		double pointsPerTimeStep = (double) (entity.getPoints().size()) / entity.getMaxServiceTimeSteps()
				/ Constants.PAINT_STEPS;
		int max = (int) (pointsPerTimeStep * (timeStep % Constants.PAINT_STEPS)
				+ ((entity.getCurrentServiceTimeStep() - 1) * (pointsPerTimeStep * Constants.PAINT_STEPS)));
		int min = (int) Math.round(max - pointsPerTimeStep * LENGTH);

		// System.out.println("MINMAX: "+ min + " - " + max);

		min = (min >= entity.getPoints().size()) ? entity.getPoints().size() - 1 : (min < 0) ? 0 : min;
		
		// Edge should contain min. 2 Positions (Start + End)
		GeoPosition last = entity.getPoints().get(min).getPosition();
		for (int i = min; i < max && i < entity.getPoints().size(); i++) {
			MapNode node = entity.getPoints().get(i);
			GeoPosition current = node.getPosition();

			drawRoutePart(g, map, last, current, node.capWork[timeStepBig][0], node.capWork[timeStepBig][1]);

			last = current;
		}

	}

	private void drawGreyEdgeLine(Graphics2D g, MyMap map, NodeEdge edge) {
		g.setColor(Color.GRAY);
		g.setStroke(new BasicStroke(1.2f));
		g.draw(edge.getShape(map));
	}

	private Color lastCol = Color.GRAY;

	private void drawRoutePart(Graphics2D g, MyMap map, GeoPosition from, GeoPosition to, long capacity,
			long workload) {
		// modifying the graphics component is work intensive,
		Color curCol = DefaultRoutePainter.calculateColor(workload, capacity);
		if (!curCol.equals(lastCol)) {
			g.setColor(curCol);
			g.setStroke(new BasicStroke(capacity / 250f));
		}

		DefaultRoutePainter.drawNormalLine(g, map, from, to);
	}

	private void calcOnlyVisibleEdges(Graphics2D g, MyMap map) {
		Rectangle rect = map.getViewportBounds();
		// XXX Magic Numbers for Debug
		GeoPosition viewportStart = map.getGeoPos(rect.getMinX(), rect.getMinY() );
		GeoPosition viewportEnd = map.getGeoPos(rect.getMaxX() , rect.getMaxY());
		routeController.excludeNonVisiblePointFromPaintRoutes(viewportStart, viewportEnd);
		if (Constants.debugInfos && Constants.drawOnlyViewport) {
			g.setColor(Color.MAGENTA);
			g.drawRect((int)rect.getMinX() + 300, (int)rect.getMinY() + 150,
					(int)rect.getMaxX() - 300 - ((int)rect.getMinX() + 300), (int)rect.getMaxY() - 150 - ((int)rect.getMinY() + 150));
		}
	}

}
