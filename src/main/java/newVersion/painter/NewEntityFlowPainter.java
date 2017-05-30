package newVersion.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import main.RouteController;
import models.Constants;
import models.Edge;
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
		int entityStepCurrent = timeStep / Constants.PAINT_STEPS;
		
		
		for (int i = 0; i < routeController.getRoute().size(); i++) {
			
			
			NodeEdge edge = (NodeEdge) routeController.getRoute().get(i); // XXX potential problem source
			GeoPosition last = edge.getStart();
			for (MapNode node : edge.getPoints()) {
				
				GeoPosition current = node.getPosition();
				drawRoutePart(g, map, last, current, node.capWork[entityStepCurrent][0], node.capWork[entityStepCurrent][1]);
				last = current;
			}
			
		}
		
	}

	private void drawRoutePart(Graphics2D g, MyMap map, GeoPosition from, GeoPosition to, long capacity, long workload) {
		g.setColor(DefaultRoutePainter.calculateColor(workload, capacity));
		g.setStroke(new BasicStroke(capacity / 250f));
		
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
