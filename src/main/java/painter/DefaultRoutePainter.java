package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import main.MainController;
import main.RouteController;
import models.Constants;
import models.Edge;
import models.EdgeType;
import models.MapRoute;

/**
 * Paints a route
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 * 
 */
public class DefaultRoutePainter implements IRoutePainter {

	private int currentTimeStep = 0;
	private RouteController routeController;

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.IRoutePainter#setTimeStep(int)
	 */
	@Override
	public void setTimeStep(int time) {
		currentTimeStep = time;
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

	/**
	 * @param g
	 *            the graphics object
	 * @param map
	 *            the map
	 */
	@Override
	public void drawRoute(Graphics2D g, MyMap map) {
		int i = 0;
		List<Edge> route = routeController.getRoute();
		showSearchRadius(g, map, route);
		for (Edge edge : route) {

			if (edge.getType().equals(EdgeType.VESSEL)) {
				System.out.println("ERROR wrong edge");
			}

			i++;
			List<GeoPosition> points = edge.getPositions();
			GeoPosition last = null;

			for (int j = 0; j < points.size(); j++) {
				GeoPosition point = points.get(j);
				modifyGraphicsForStep(g, edge, j, currentTimeStep);

				if (last == null) {
					last = point;
					showContactPoints(g, map, edge, j);
					continue;
				}

				Point2D[] stEnPoints = drawNormalLine(g, map, last, point);
				last = point; // Important, else every edge consists of lines
								// from edge start to every step

				// Contact Points - not finished
				showContactPoints(g, map, edge, j);
				showPointDescriptionForStep(g, map, i, edge, j, stEnPoints[0], stEnPoints[1]);
			}
		}
	}

	public static Point2D[] drawNormalLine(Graphics2D g, MyMap map, GeoPosition from, GeoPosition to) {
		// convert geo-coordinate to world bitmap pixel
		Point2D startPt2D = map.getTileFactory().geoToPixel(from, map.getZoom());
		Point2D endPt2D = map.getTileFactory().geoToPixel(to, map.getZoom());

		g.drawLine((int) startPt2D.getX(), (int) startPt2D.getY(), (int) endPt2D.getX(), (int) endPt2D.getY());

		
		return new Point2D[] { startPt2D, endPt2D };
	}

	private static void modifyGraphicsForStep(Graphics2D g, Edge edge, int j, int currentTimeStep) {
		long currentWorkload = 0;
		long currentCapacity = 0;
		long nextWorkload = 0;
		long nextCapacity = 0;

		int entityStepCurrent = currentTimeStep / Constants.PAINT_STEPS_COUNT;
		int entityStepNext = entityStepCurrent + 1;

		if (MapRoute.class.isInstance(edge)) {
			MapRoute mapEdge = (MapRoute) edge;
			currentWorkload = mapEdge.getWorkloadForPoint(currentTimeStep, j);
			currentCapacity = mapEdge.getCapacityForPoint(currentTimeStep, j);

			nextWorkload = mapEdge.getWorkloadForPoint(currentTimeStep, j + 1);
			nextCapacity = mapEdge.getCapacityForPoint(currentTimeStep, j + 1);
		} else {
			currentWorkload = edge.getWorkload(entityStepCurrent);
			currentCapacity = edge.getCapacity(entityStepCurrent);

			nextWorkload = edge.getWorkload(entityStepNext);
			nextCapacity = edge.getCapacity(entityStepNext);
		}

		double stepFactor = currentTimeStep % Constants.PAINT_STEPS_COUNT / (double) Constants.PAINT_STEPS_COUNT;

		long valCapacity = (long) (nextCapacity * stepFactor + currentCapacity * (1 - stepFactor));
		long valWorkload = (long) (nextWorkload * stepFactor + currentWorkload * (1 - stepFactor));

//		System.out.println(String.format("STEP: %f - %d, CUR: %d, N: %d, VAL: %d", stepFactor, currentTimeStep,
//				currentCapacity, nextCapacity, valCapacity));

		if (valCapacity == 0) {
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(1.2f));
		} else {
			Color lineColor = calculateColor(valWorkload, valCapacity);
			g.setColor(lineColor);
			g.setStroke(new BasicStroke((float) (valCapacity / 75)));
		}
	}

	private void showPointDescriptionForStep(Graphics2D g, JXMapViewer map, int i, Edge edge, int j, Point2D startPt,
			Point2D endPt) {
		if (Constants.debugInfos && (map.getZoom() < Constants.DEBUG_ZOOM)) {
			String index = i + "";

			final int circleRadius = 10;
			g.setColor(calculateColor(j, edge.getPositions().size() + 1));
			g.fillOval((int) startPt.getX() - (circleRadius / 2), (int) startPt.getY() - (circleRadius / 2),
					circleRadius, circleRadius);
			g.setColor(Color.RED);
			g.drawString(index + " - " + j, (int) startPt.getX() + i, (int) startPt.getY());
			g.drawString(index, (int) endPt.getX() + i, (int) endPt.getY());
		}
		// Edge info is always drawn at the start point of the edge
		if (edge.getInfo() != null) {
			g.setColor(Color.RED);
			g.setFont(new Font("default", Font.BOLD, 16));
			g.drawString(edge.getInfo(), (int) endPt.getX(), (int) endPt.getY() + 15);
		}
	}

	private void showContactPoints(Graphics2D g, JXMapViewer map, Edge edge, int j) {
		if (Constants.debugInfos) {
			if (MapRoute.class.isInstance(edge)) {

				// GeoPosition contactPoint = ((MapEdge)
				// edge).points.get(j).contactPoint;
				// GeoPosition point = ((MapEdge)
				// edge).points.get(j).getPosition();

				GeoPosition contactPoint = ((MapRoute) edge).getContactPointForIndex(j);
				GeoPosition point = ((MapRoute) edge).getPositions().get(j);

				if (contactPoint != null) {
					Point2D startPt2D = map.getTileFactory().geoToPixel(point, map.getZoom());
					Point2D contactPointP = map.getTileFactory().geoToPixel(contactPoint, map.getZoom());

					if (map.getZoom() < Constants.DEBUG_ZOOM) {
						g.setColor(Color.BLUE);
						final int circleRadius = 10;
						g.fillOval((int) contactPointP.getX(), (int) contactPointP.getY(), circleRadius, circleRadius);
					}

					g.drawLine((int) contactPointP.getX(), (int) contactPointP.getY(), (int) startPt2D.getX(),
							(int) startPt2D.getY());
				}
			}
		}
	}

	private void showSearchRadius(Graphics2D g, MyMap map, List<Edge> route) {
		if (map.getZoom() < Constants.DEBUG_ZOOM && Constants.debugInfos) {
			// for (Edge edge : route) {
			for (int i = 0; i < route.size(); i++) {
				Edge edge = route.get(i);
				if (MapRoute.class.isInstance(edge)) {

					for (int j = 0; j < edge.getPositions().size(); j += edge.getPositions().size() - 1) {
						GeoPosition p = ((MapRoute) edge).getPoints().get(j).getPosition();
						Point2D pp = map.getTileFactory().geoToPixel(p, map.getZoom());
						// double distance = 0.05;
						double distance = MainController.contactSearchDistance * 2.75;
						GeoPosition pcopy = new GeoPosition(p.getLatitude() + distance, p.getLongitude() + distance);
						int circleRadius = (int) Math
								.abs((map.getTileFactory().geoToPixel(pcopy, map.getZoom())).getX() - pp.getX());
						g.setColor(Color.CYAN);
						g.setStroke(new BasicStroke(5f));
						g.drawOval((int) pp.getX() - (circleRadius / 2), (int) pp.getY() - (circleRadius / 2),
								circleRadius, circleRadius);
					}
				}
			}
		}
	}

	public static Color calculateColor(long workload, long capacity) {
		Color color1 = Color.GREEN;
		Color color2 = Color.RED;
		float ratio = (float) workload / (float) capacity;
		int red = (int) (color2.getRed() * ratio + color1.getRed() * (1 - ratio));
		int green = (int) (color2.getGreen() * ratio + color1.getGreen() * (1 - ratio));
		int blue = (int) (color2.getBlue() * ratio + color1.getBlue() * (1 - ratio));
		return new Color(red, green, blue);
	}

}