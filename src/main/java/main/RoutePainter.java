package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

/**
 * Paints a route
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 * 
 */
public class RoutePainter implements Painter<JXMapViewer> {

	private Color borderColor = Color.BLACK;
	private Color lineColor = Color.RED;
	private boolean antiAlias = true;

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

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		// border drawing
		g.setColor(borderColor);
		g.setStroke(new BasicStroke((float) 2.5));
		drawRoute(g, map);

		// line drawing
		g.setColor(lineColor);
		g.setStroke(new BasicStroke(2));
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
		for (Edge edge : route) {
			// convert geo-coordinate to world bitmap pixel
			Point2D startPt = map.getTileFactory().geoToPixel(edge.start, map.getZoom());
			Point2D endPt = map.getTileFactory().geoToPixel(edge.end, map.getZoom());
			g.drawLine((int) startPt.getX(), (int) startPt.getY(), (int) endPt.getX(), (int) endPt.getY());
		}
	}
}