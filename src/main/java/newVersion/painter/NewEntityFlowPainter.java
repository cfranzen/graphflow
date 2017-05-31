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
		int timeStepBig = timeStep / Constants.PAINT_STEPS;

		if (timeStep % Constants.PAINT_STEPS == 0) {
			for (int i = 0; i < routeController.getRoute().size(); i++) {
	
				NodeEdge edge = (NodeEdge) routeController.getRoute().get(i);
				int serviceTime = (int)edge.getServiceTime(timeStepBig);
	
				
				FlowEntity e = new FlowEntity(serviceTime, timeStepBig, edge);
				entities.add(e);
			}

			List<FlowEntity> newEntityList = new ArrayList<>();
			for (FlowEntity flowEntity : entities) {
				if (flowEntity.next()) {
					drawEntity(g, map, timeStepBig, flowEntity.edge);
					newEntityList.add(flowEntity);
				} else {
					
				}
			}
			entities = newEntityList;
		} else {
			for (FlowEntity flowEntity : entities) {
				drawEntity(g, map, timeStepBig, flowEntity.edge);
			}
		}

		
		
	}

	private void drawEntity(Graphics2D g, MyMap map, int timeStepBig, NodeEdge edge) {
		GeoPosition last = edge.getStart();
		for (MapNode node : edge.getPoints()) {

			GeoPosition current = node.getPosition();
			drawRoutePart(g, map, last, current, node.capWork[timeStepBig][0], node.capWork[timeStepBig][1]);
			last = current;
		}
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
			g.fillOval((int) rect.getMinX() + 300, (int) rect.getMinY() + 150, 10, 10);
			g.fillOval((int) rect.getMinX() + 300, (int) rect.getMaxY() - 150, 10, 10);
			g.fillOval((int) rect.getMaxX() - 300, (int) rect.getMaxY() - 150, 10, 10);
			g.fillOval((int) rect.getMaxX() - 300, (int) rect.getMinY() + 150, 10, 10);
		}
	}

}
