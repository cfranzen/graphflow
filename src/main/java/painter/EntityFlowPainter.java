package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import main.RouteController;
import models.Constants;
import models.Edge;
import models.MapRoute;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class EntityFlowPainter implements IRoutePainter {

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
		Rectangle rect = map.getViewportBounds();
		// XXX Magic Numbers for Debug
		GeoPosition viewportStart = map.getGeoPos(rect.getMinX() + 300, rect.getMinY() + 150);
		GeoPosition viewportEnd = map.getGeoPos(rect.getMaxX() - 300, rect.getMaxY() - 150);
		routeController.refreshPaintRoutes(viewportStart, viewportEnd);

		if (Constants.debugInfos) {
			int size = 0;
			for (Edge edge : routeController.getPaintRoute()) {
				size += edge.getPositions().size();
			}
			// System.out.println("Edge: " +
			// routeController.getPaintRoute().size() + " - Points: " + size);
			g.setColor(Color.MAGENTA);
			g.fillOval((int) rect.getMinX() + 300, (int) rect.getMinY() + 150, 10, 10);
			g.fillOval((int) rect.getMinX() + 300, (int) rect.getMaxY() - 150, 10, 10);
			g.fillOval((int) rect.getMaxX() - 300, (int) rect.getMaxY() - 150, 10, 10);
			g.fillOval((int) rect.getMaxX() - 300, (int) rect.getMinY() + 150, 10, 10);
		}

		for (Edge edge : routeController.getPaintRoute()) {
			int entityStepCurrent = timeStep / Constants.PAINT_STEPS;
			List<GeoPosition> points = edge.getPositions();
			double serviceTimeSteps = edge.getServiceTime(entityStepCurrent);
			serviceTimeSteps = serviceTimeSteps < 1 ? 1 : serviceTimeSteps;

			int pointsPerStep = (int) (points.size() / serviceTimeSteps);
			int from = 0;
			int to = pointsPerStep;

			// if (true) return;

			for (int i = 0; i < serviceTimeSteps; i++) {
				if (i > 2)
					return;

				double stepFactor = timeStep % Constants.PAINT_STEPS / (double) Constants.PAINT_STEPS;
				from = (int) (pointsPerStep * i);
				to = (int) (from + (pointsPerStep * stepFactor) + 1);

				if (Constants.debugInfos) {
					GeoPosition geoPos;
					if (pointsPerStep * (i + 1) < points.size()) {
						geoPos = points.get((int) (pointsPerStep * (i + 1)));
					} else {
						geoPos = points.get(points.size() - 1);
					}
					Point2D point = map.getTileFactory().geoToPixel(geoPos, map.getZoom());
					g.setColor(Color.BLUE);
					g.drawOval((int) point.getX(), (int) point.getY(), 15, 15);
				}

				if (edge instanceof MapRoute) {
					mapRoutePainter(g, map, (MapRoute) edge, entityStepCurrent, i, pointsPerStep);
				} else {
					long workload = edge.getWorkload(entityStepCurrent - i);
					long workloadBefore = edge.getWorkload(entityStepCurrent - i - 1);
					long capacity = edge.getCapacity(entityStepCurrent - i);
					long capacityBefore = edge.getCapacity(entityStepCurrent - i - 1);

					drawRoutePart(g, map, workload, capacity, points, from, to);
					drawRoutePart(g, map, workloadBefore, capacityBefore, points, to, points.size());
				}
			}

		}

	}

	/**
	 * 
	 */
	private void mapRoutePainter(Graphics2D g, MyMap map, MapRoute edge, int currentTimeStep, int serviceTimeStep,
			int pointsPerStep) {
		double stepFactor = timeStep % Constants.PAINT_STEPS / (double) Constants.PAINT_STEPS;
		int from = (int) (pointsPerStep * serviceTimeStep);
		int to = (int) (from + (pointsPerStep * stepFactor) + 1);

		drawMapRoutePart(g, map, edge, from, to, currentTimeStep - serviceTimeStep);
		drawMapRoutePart(g, map, edge, to, edge.getPositions().size(), currentTimeStep - serviceTimeStep - 1);
	}

	/**
	 * @param g
	 * @param map
	 * @param points
	 * @param from
	 * @param to
	 */
	private void drawMapRoutePart(Graphics2D g, MyMap map, MapRoute edge, int from, int to, int currentTimeStep) {
		GeoPosition last = null;
		for (int i = from; i < to && i < edge.getPositions().size(); i++) {
			long capacity = edge.getCapacityForPoint(currentTimeStep, i);
			long workload = edge.getWorkloadForPoint(currentTimeStep, i);
			if (workload == 0) {
				continue;
			}

			Color lineColor = DefaultRoutePainter.calculateColor(workload, capacity);
			g.setColor(lineColor);
			g.setStroke(new BasicStroke(4f));
			GeoPosition point = edge.getPositions().get(i);
			if (last == null) {
				last = point;
				continue;
			}
			DefaultRoutePainter.drawNormalLine(g, map, last, point);
			last = point;
		}

	}

	public void drawRoutePart(Graphics2D g, MyMap map, long workload, long capacity, List<GeoPosition> points,
			int fromIndex, double toIndex) {
		if (capacity == 0) { // TODO Gradient
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(1.2f));
		} else {
			Color lineColor = DefaultRoutePainter.calculateColor(workload, capacity);
			g.setColor(lineColor);

			g.setStroke(new BasicStroke(4f));
			// g.setStroke(new BasicStroke((float) (capacity / 75)));// TODO
			// Gradient
		}
		GeoPosition last = null;
		for (int i = fromIndex; i < toIndex && i < points.size(); i++) {
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
	 * @see painter.IRoutePainter#setRouteController(main.RouteController)
	 */
	@Override
	public void setRouteController(RouteController routeController) {
		this.routeController = routeController;
	}

}
