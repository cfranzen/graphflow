package newVersion.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import main.RouteController;
import models.Constants;
import models.Edge;
import newVersion.models.FlowEntity;
import newVersion.models.MapNode;
import newVersion.models.NodeEdge;
import painter.DefaultRoutePainter;
import painter.IRoutePainter;

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
			drawGreyEdgeLine(g, map, (NodeEdge) edge);
		}

		int timeStepBig = timeStep / Constants.PAINT_STEPS;

		// Every bigger time step new Entities are starting from the nodes
		if (timeStep % Constants.PAINT_STEPS == 0) {
			// Create land entities
			for (int i = 0; i < routeController.getRoute().size(); i++) {

				NodeEdge edge = (NodeEdge) routeController.getRoute().get(i);
				int serviceTime = (int) edge.getServiceTime(timeStepBig);

				FlowEntity e = new FlowEntity(serviceTime, timeStepBig, edge);
				entities.add(e);
			}
			
			// Create sea entities
			for (int i = 0; i < routeController.getSeaRoute().size(); i++) {
				// TODO create sea enitites
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
			drawEntity(g, map, timeStepBig, flowEntity);
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
	private void drawEntity(Graphics2D g, MyMap map, int timeStepBig, FlowEntity entity) {
		final int LENGTH = 10; // Length of the entity line in relation to the
								// edge point count

		double pointsPerTimeStep = (double) (entity.edge.getPoints().size()) / entity.maxServiceTimeSteps
				/ Constants.PAINT_STEPS;
		int max = (int) (pointsPerTimeStep * (timeStep % Constants.PAINT_STEPS)
				+ ((entity.currentServiceTimeStep - 1) * (pointsPerTimeStep * Constants.PAINT_STEPS)));
		int min = (int) Math.round(max - pointsPerTimeStep * LENGTH);

		// System.out.println("MINMAX: "+ min + " - " + max);

		min = (min >= entity.edge.getPoints().size()) ? entity.edge.getPoints().size() - 1 : (min < 0) ? 0 : min;

		GeoPosition last = entity.edge.getPoints().get(min).getPosition();
		for (int i = min; i < max && i < entity.edge.getPoints().size(); i++) {
			MapNode node = entity.edge.getPoints().get(i);
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
		GeoPosition viewportStart = map.getGeoPos(rect.getMinX() + 300, rect.getMinY() + 150);
		GeoPosition viewportEnd = map.getGeoPos(rect.getMaxX() - 300, rect.getMaxY() - 150);
		routeController.refreshPaintRoutes(viewportStart, viewportEnd);
		if (Constants.debugInfos) {
			g.setColor(Color.MAGENTA);
			g.drawRect((int)rect.getMinX() + 300, (int)rect.getMinY() + 150,
					(int)rect.getMaxX() - 300 - ((int)rect.getMinX() + 300), (int)rect.getMaxY() - 150 - ((int)rect.getMinY() + 150));
//			g.fillOval((int) rect.getMinX() + 300, (int) rect.getMinY() + 150, 10, 10);
//			g.fillOval((int) rect.getMinX() + 300, (int) rect.getMaxY() - 150, 10, 10);
//			g.fillOval((int) rect.getMaxX() - 300, (int) rect.getMaxY() - 150, 10, 10);
//			g.fillOval((int) rect.getMaxX() - 300, (int) rect.getMinY() + 150, 10, 10);
		}
	}

}
