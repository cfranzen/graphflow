package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import main.MainController;
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

	public final static int debugZoomLevel = 6;

	private boolean antiAlias = true;
	private int currentTimeStep = 0;

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
	 * @see gui.IRoutePainter#paint(java.awt.Graphics2D,
	 * org.jxmapviewer.JXMapViewer, int, int)
	 */
	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawRoute(g, (MyMap) map);
		g.dispose();
	}

	/**
	 * @param g
	 *            the graphics object
	 * @param map
	 *            the map
	 */
	private void drawRoute(Graphics2D g, MyMap map) {
		int i = 0;
		List<Edge> route = MainController.getInstance().getRouteController().getRoute();
		showSearchRadius(g, map, route);
		for (Edge edge : route) {
			
			// XXX debug, do not draw sea solution edges
			if (edge.getType().equals(EdgeType.VESSEL)) {
				System.out.println("ERROR wrong edge");	
			}
			
			i++;
			List<GeoPosition> points = edge.getPositions();
			GeoPosition last = null;

			for (int j = 0; j < points.size(); j++) {
				GeoPosition point = points.get(j);
				modifyGraphicsForStep(g, edge, j);

				if (last == null) {
					last = point;
					showContactPoints(g, map, edge, j);
					continue;
				}
				
				

				Point2D[] stEnPoints = drawNormalLine(g, map, last, point);
				last = point; // Important, else every edge consists of lines from edge start to every step

				// Contact Points - not finished
				showContactPoints(g, map, edge, j);
				showPointDescriptionForStep(g, map, i, edge, j, stEnPoints[0], stEnPoints[1]);
			}
		}
	}

	static Point2D[] drawNormalLine(Graphics2D g, MyMap map, GeoPosition from, GeoPosition to) {
		// convert geo-coordinate to world bitmap pixel
		Point2D startPt2D = map.getTileFactory().geoToPixel(from, map.getZoom());
		Point2D endPt2D = map.getTileFactory().geoToPixel(to, map.getZoom());

		g.drawLine((int) startPt2D.getX(), (int) startPt2D.getY(), (int) endPt2D.getX(), (int) endPt2D.getY());

		return new Point2D[] { startPt2D, endPt2D };
	}

	private void modifyGraphicsForStep(Graphics2D g, Edge edge, int j) {
		long currentWorkload = 0;
		long currentCapacity = 0;
		if (MapRoute.class.isInstance(edge)) {
			MapRoute mapEdge = (MapRoute) edge;
			currentWorkload = mapEdge.getWorkloadForPoint(currentTimeStep, j);
			currentCapacity = mapEdge.getCapacityForPoint(currentTimeStep, j);
		} else {
			currentWorkload = edge.getWorkload(currentTimeStep);
			currentCapacity = edge.getCapacity(currentTimeStep);
		}

		if (currentCapacity == 0) {
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(1.2f));
		} else {
			Color lineColor = calculateColor(currentWorkload, currentCapacity);
			g.setColor(lineColor);
			if (edge.getType().equals(EdgeType.VESSEL)) {
				g.setStroke(new BasicStroke(currentCapacity / 500));
				g.setColor(Color.PINK);
			} else {
				g.setStroke(new BasicStroke((float) (currentCapacity / 100)));
			}
		}
	}

	private void showPointDescriptionForStep(Graphics2D g, JXMapViewer map, int i, Edge edge, int j, Point2D startPt,
			Point2D endPt) {
		if (MainController.debugInfos && (map.getZoom() < debugZoomLevel)) {
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
		if (MainController.debugInfos) {
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

					if (map.getZoom() < debugZoomLevel) {
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
		if (map.getZoom() < debugZoomLevel && MainController.debugInfos) {
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

	static Color calculateColor(long workload, long capacity) {
		Color color1 = Color.GREEN;
		Color color2 = Color.RED;
		float ratio = (float) workload / (float) capacity;
		int red = (int) (color2.getRed() * ratio + color1.getRed() * (1 - ratio));
		int green = (int) (color2.getGreen() * ratio + color1.getGreen() * (1 - ratio));
		int blue = (int) (color2.getBlue() * ratio + color1.getBlue() * (1 - ratio));
		return new Color(red, green, blue);
	}
}