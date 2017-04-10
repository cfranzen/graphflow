package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.log4j.chainsaw.Main;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.MainController;
import models.Edge;
import models.EdgeType;
import models.HighResEdge;
import models.MapRoute;
import models.SeaEdge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaRoutePainter implements IRoutePainter {

	private static final Logger logger = LoggerFactory.getLogger(SeaRoutePainter.class);

	private int circleDiameter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.IRoutePainter#setTimeStep(int)
	 */
	@Override
	public void setTimeStep(int time) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.IRoutePainter#paint(java.awt.Graphics2D,
	 * org.jxmapviewer.JXMapViewer, int, int)
	 */
	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		g.setStroke(new BasicStroke(3));
		g.setColor(Color.PINK);

		
		GeoPosition pos = new GeoPosition(0, 0);
		Point2D p = map.getTileFactory().geoToPixel(pos, map.getZoom());

		double distance = 2.75;
		GeoPosition pcopy = new GeoPosition(pos.getLatitude() + distance, pos.getLongitude() + distance);
		circleDiameter = (int) Math.abs((map.getTileFactory().geoToPixel(pcopy, map.getZoom())).getX() - p.getX());
		
		
		 List<SeaEdge> lines = calcSeaLines(g, (MyMap) map);


		if (MainController.debugInfos) {
			drawSeaRoute(g, (MyMap) map);
			drawNodeIds(g);
		}
		
	}

	/**
	 * @param g
	 * @param map
	 * @return
	 */
	private List<SeaEdge> calcSeaLines(Graphics2D g, MyMap map) {
		List<Edge> route = MainController.getInstance().getRouteController().getSeaRoute();
		List<SeaEdge> drawEdges = new ArrayList<>();

		for (int i = 0; i < route.size(); i++) {
			if (route.get(i) instanceof HighResEdge == false)
				return null;
			HighResEdge edge = (HighResEdge) route.get(i);
			
			GeoPosition lastPos = null;
			Point2D lastCircleP = null;
			for (GeoPosition pos : edge.getPositions()) {
				if (lastPos == null) {
					lastPos = pos;
					lastCircleP = map.getTileFactory().geoToPixel(pos, map.getZoom());
					continue;
				}

				Point2D lastPosP = map.getTileFactory().geoToPixel(lastPos, map.getZoom());
				Point2D posP = map.getTileFactory().geoToPixel(pos, map.getZoom());
				// Konvertierung verlustbehaftet
				
				
				Point2D circlePIn = getCirclePoint(posP, lastPosP);
				Point2D circlePOut = getCirclePoint(lastPosP, posP);

				Path2D path = new Path2D.Double();
				path.moveTo(lastCircleP.getX(), lastCircleP.getY());
				path.quadTo(lastPosP.getX(), lastPosP.getY(), circlePIn.getX(), circlePIn.getY());
				path.lineTo(circlePOut.getX(), circlePOut.getY());

				SeaEdge seaEdge = new SeaEdge(lastPos, pos);
				seaEdge.shape = path;
				drawEdges.add(seaEdge);

				lastPos = pos;
				lastCircleP = circlePOut;
			}

		}

		g.setColor(Color.GREEN);
		for (SeaEdge seaEdge : drawEdges) {
			g.draw(seaEdge.shape);
					
		}
		
//		for (Edge edge : MainController.getInstance().getRouteController().getSeaRoute()) {
//			Point2D pD = map.getTileFactory().geoToPixel(edge.getDest(), map.getZoom());
//			Point2D pS = map.getTileFactory().geoToPixel(edge.getStart(), map.getZoom());
//			g.setColor(Color.RED);
//			g.drawOval((int) pS.getX()-13,(int) pS.getY()-13, 26, 26);
//			g.setColor(Color.BLUE);
//			g.drawOval((int) pD.getX()-8,(int) pD.getY()-8, 16, 16);
//		}

		return null;
	}

	private void drawSeaRoute(Graphics2D g, MyMap map) {
		g.setColor(Color.pink);
		List<Edge> route = MainController.getInstance().getRouteController().getSeaRoute();

		for (Edge edge : route) {
			if (edge.getPositions().size() <= 3) {
				DefaultRoutePainter.drawNormalLine(g, map, edge.getStart(), edge.getDest());
			} else {

				GeoPosition last = null;
				for (GeoPosition pos : edge.getPositions()) {
					if (last == null) {
						last = pos;
						continue;
					}
					if (checkEquatorEdge(last, pos)) {
						drawEquatorEdge(g, map, last, pos);
					} else {
						DefaultRoutePainter.drawNormalLine(g, map, last, pos);
						last = pos;
					}
				}
			}
		}

	}

	/**
	 * 
	 * @param p1
	 *            startpoint line
	 * @param p2
	 *            center of circle
	 * @return
	 */
	private Point2D getCirclePoint(Point2D p1, Point2D p2) {
		// // gerade: y = mg * x + b | mg=y2-y1/x2-x1, b = y1
		double r = circleDiameter / 2;

		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();

		double mg = (y2 - y1) / (x2 - x1);
		double b = -mg * x1 + y1;
		double b2 = -mg * x2 + y2;
		debugOut("b: " + b + " - b2: " + b2);

		double m1 = p2.getX();
		double m2 = p2.getY();

		double p = (mg * b - m1 - (m2 * mg)) / (1 + (mg * mg));// p/2
		double q = ((m1 * m1) + (b * b) - 2 * m2 * b + (m2 * m2) - (r * r)) / (1 + (mg * mg));

		debugOut("p: " + p + " - q: " + q);

		double diskriminante = (p * p) - q;
		debugOut("diskriminante: " + diskriminante);

		double xres1 = -p + Math.sqrt(diskriminante);
		double xres2 = -p - Math.sqrt(diskriminante);

		double yres1 = mg * xres1 + b;
		double yres2 = mg * xres2 + b;

		debugOut("P1: " + xres1 + " - " + yres1);
		debugOut("P2: " + xres2 + " - " + yres2);
		debugOut("PStar: " + x1 + " - " + y1);
		debugOut("PDest: " + x2 + " - " + y2 + " | r:" + r);

		Point2D cp1 = new Point((int) xres1, (int) yres1);
		Point2D cp2 = new Point((int) xres2, (int) yres2);

		Point2D result = cp1;
		if (dist(p1, cp1) > dist(p1, cp2)) {
			result = cp2;
		}

		return result;
	}


	/**
	 * @param p1
	 * @param cp1
	 * @return
	 */
	private double dist(Point2D p1, Point2D p2) {
		double x = p1.getX() - p2.getX();
		double y = p1.getY() - p2.getY();
		return Math.sqrt((x * x) + (y * y));
	}

	private void debugOut(String line) {
		debugOut(line, false);
	}

	private void debugOut(String line, boolean useLogger) {
		if (MainController.debugInfos ) {
			if (useLogger) {
				if (logger.isDebugEnabled()) {
					logger.debug(line);
				} else {
					logger.info(line);
				}
			} else {
				System.out.println(line);
			}
		}
	}

	private void drawNodeIds(Graphics2D g) {
		g.setColor(Color.red);
		g.setFont(new Font("default", Font.BOLD, 16));
		for (Edge edge : MainController.getInstance().getSeaController().getEdges()) {
			MyMap map = MainController.getInstance().getMapViewer();
			GeoPosition pos = edge.getDest();
			Point2D p = map.getTileFactory().geoToPixel(pos, map.getZoom());

			double distance = 2.75;
			GeoPosition pcopy = new GeoPosition(pos.getLatitude() + distance, pos.getLongitude() + distance);
			circleDiameter = (int) Math.abs((map.getTileFactory().geoToPixel(pcopy, map.getZoom())).getX() - p.getX());

			g.drawOval((int) (p.getX() - (circleDiameter / 2)), (int) (p.getY() - (circleDiameter / 2)), circleDiameter,
					circleDiameter);

			final int circle = 5;
			g.fillOval((int) (p.getX() - (circle / 2)), (int) (p.getY() - (circle / 2)), circle, circle);

			if (edge.getInfo() != null) {
				g.drawString(edge.getInfo(), (int) p.getX() + 15, (int) p.getY());
			}
		}

	}

	private boolean checkEquatorEdge(GeoPosition point, GeoPosition last) {
		return ((last.getLongitude() > 90) && (point.getLongitude() < -30)
				|| (last.getLongitude() < -30) && (point.getLongitude() > 90));
	}

	/**
	 * @return
	 * 
	 */
	private Point2D[] drawEquatorEdge(Graphics2D g, MyMap map, GeoPosition from, GeoPosition to) {
		// init vars
		GeoPosition pos1 = from;
		GeoPosition pos2 = to;

		if (from.getLongitude() > 90) {
			pos1 = from;
			pos2 = to;
		} else {
			pos1 = to;
			pos2 = from;
		}

		// draw first part
		GeoPosition pointPos = new GeoPosition(pos2.getLatitude(), (360 + pos2.getLongitude()));
		DefaultRoutePainter.drawNormalLine(g, map, pos1, pointPos);

		// draw second part
		pointPos = new GeoPosition(pos1.getLatitude(), (-360 + pos1.getLongitude()));
		return DefaultRoutePainter.drawNormalLine(g, map, pos2, pointPos);
	}

}
