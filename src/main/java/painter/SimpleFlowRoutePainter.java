package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import main.RouteController;
import models.Constants;
import models.Edge;
import newVersion.painter.IRoutePainter;

/**
 * Visualizes the current capacity and workload as flow from start to end-point
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SimpleFlowRoutePainter implements IRoutePainter {

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
	 * @see painter.IRoutePainter#drawRoute(java.awt.Graphics2D, gui.MyMap)
	 */
	@Override
	public void drawRoute(Graphics2D g, MyMap map) {
		List<Edge> route = routeController.getRoute();
		for (Edge edge : route) {
			int entityStepCurrent = timeStep / Constants.PAINT_STEPS_COUNT;
			int entityStepNext = entityStepCurrent + 1;
			long currentWorkload = edge.getWorkload(entityStepCurrent);
			long currentCapacity = edge.getCapacity(entityStepCurrent);
			long nextWorkload = edge.getWorkload(entityStepNext);
			long nextCapacity = edge.getCapacity(entityStepNext);

			List<GeoPosition> points = edge.getPositions();
			double stepFactor = timeStep % Constants.PAINT_STEPS_COUNT / (double) Constants.PAINT_STEPS_COUNT;
			drawRoutePart(g, map, nextWorkload, nextCapacity, points, 0, points.size() * stepFactor);

			drawRoutePart(g, map, currentWorkload, currentCapacity, points, (int) (points.size() * stepFactor),
					points.size());

		}

	}

	public void drawRoutePart(Graphics2D g, MyMap map, long workload, long capacity, List<GeoPosition> points,
			int fromIndex, double toIndex) {
		if (capacity == 0) { 
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(1.2f));
		} else {
			Color lineColor = DefaultRoutePainter.calculateColor(workload, capacity);
			g.setColor(lineColor);
			g.setStroke(new BasicStroke((float) (capacity / 75)));
		}
		GeoPosition last = null;
		for (int i = fromIndex; i < toIndex; i++) {
			GeoPosition point = points.get(i);
			if (last == null) {
				last = point;
				continue;
			}
			DefaultRoutePainter.drawNormalLine(g, map, last, point);
			last = point; // Important, else every edge consists of lines
							// from edge start to every step
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#setRoute(java.util.List)
	 */
	@Override
	public void setRouteController(RouteController routeController) {
		this.routeController = routeController;
	}

}
