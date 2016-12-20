package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import models.Edge;
import models.EdgeType;
import models.MapEdge;

/**
 * Paints a route
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 * 
 */
public class RoutePainter implements Painter<JXMapViewer> {

	private boolean antiAlias = true;
	private boolean showPoints = false;
	boolean showContactPoints = false;
	private int currentTimeStep = 0;

	private Collection<Edge> route;

	/**
	 * @param track
	 *            the track
	 */
	public RoutePainter(List<Edge> track) {
		// copy the list so that changes in the
		// original list do not have an effect here
		this.route = new ArrayList<Edge>(track);
	}

	/**
	 * @param route
	 *            the route to set
	 */
	public void setRoute(Collection<Edge> route) {
		this.route = route;
	}

	public void setTimeStep(int time) {
		currentTimeStep = time;
	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawRoute(g, map);
		g.dispose();
	}

	/**
	 * @param g
	 *            the graphics object
	 * @param map
	 *            the map
	 */
	private void drawRoute(Graphics2D g, JXMapViewer map) {

		

		int i = 0;
		for (Edge edge : route) {
			// int j = 0;
			i++;
			List<Double[]> points = edge.getPoints();
			Double[] last = null;
			// for (Double[] point : points) {
			for (int j = 0; j < points.size(); j++) {
				Double[] point = points.get(j);
				// j++;
				if (last == null) {
					last = point;
					continue;
				}

				// convert geo-coordinate to world bitmap pixel
				Point2D startPt2D = map.getTileFactory().geoToPixel(new GeoPosition(last[0], last[1]), map.getZoom());
				Point2D endPt2D = map.getTileFactory().geoToPixel(new GeoPosition(point[0], point[1]), map.getZoom());
				last = point;

				Point startPt = new Point((int) startPt2D.getX(), (int) startPt2D.getY());
				Point endPt = new Point((int) endPt2D.getX(), (int) endPt2D.getY());
				int currentWorkload = 0;
				int currentCapacity = 0;
				if (MapEdge.class.isInstance(edge)) {
					MapEdge e = (MapEdge)edge;
					int currentWorkload1 = e.getWorkloadForPoint(currentTimeStep, j-1);
					int currentWorkload2 = e.getWorkloadForPoint(currentTimeStep, j);
					int currentCapacity1 = e.getCapacityForPoint(currentTimeStep, j-1);
					int currentCapacity2 = e.getCapacityForPoint(currentTimeStep, j);
					currentCapacity = currentCapacity2;
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
						g.setColor(Color.BLUE);
					} else {
						g.setStroke(new BasicStroke(currentCapacity / 100));
					}
				}
				g.drawLine((int) startPt.getX(), (int) startPt.getY(), (int) endPt.getX(), (int) endPt.getY());

				// Contact Points - not finished
				// if (points.indexOf(point) <= 1 || points.indexOf(point) >=
				// points.size()-2) {
				if (showContactPoints) {
					if (MapEdge.class.isInstance(edge)) {
						g.setColor(Color.BLUE);
						Map<Edge, Double[]> edgeMap = ((MapEdge) edge).points.get(j).edgeMap;
						for (Entry<Edge, Double[]> entry : edgeMap.entrySet()) {
							Double[] p = entry.getValue();
							Point2D pp = map.getTileFactory().geoToPixel(new GeoPosition(p[0], p[1]), map.getZoom());
							g.drawLine((int) pp.getX(), (int) pp.getY(), (int) endPt2D.getX(), (int) endPt2D.getY());
						}

					}
				}

				if (showPoints) {
					String index = i + "";

					final int circleRadius = 10;
					// System.out.println(j + " - " + edge.getPoints().size());
					g.setColor(calculateColor(j, edge.getPoints().size() + 1));
					g.fillOval((int) startPt.getX(), (int) startPt.getY(), circleRadius, circleRadius);
					g.setColor(Color.RED);
					g.drawString(index, (int) startPt.getX() + i, (int) startPt.getY());
				}
			}
		}
	}

	private Color calculateColor(int workload, int capacity) {
		Color color1 = Color.GREEN;
		Color color2 = Color.RED;
		// System.out.println(String.format("Workload: %d, Capacity: %d",
		// workload, capacity));
		float ratio = (float) workload / (float) capacity;
		int red = (int) (color2.getRed() * ratio + color1.getRed() * (1 - ratio));
		int green = (int) (color2.getGreen() * ratio + color1.getGreen() * (1 - ratio));
		int blue = (int) (color2.getBlue() * ratio + color1.getBlue() * (1 - ratio));
		return new Color(red, green, blue);
	}
}