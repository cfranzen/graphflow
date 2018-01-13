package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gui.MyMap;
import main.MainController;
import models.Constants;
import models.Edge;
import models.PointPath;
import models.SeaEdge;
import newVersion.models.NodeEdge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaRoutePainter {

	private static final Logger logger = LoggerFactory.getLogger(SeaRoutePainter.class);

	public static int[] circleDiameter = new int[18];

	private static MyMap map;

	private static List<List<SeaEdge>> drawEdges = new ArrayList<>();
	// private List<Edge> route;

	/**
	 * Initalizes the circleDiameter/zoomlevel list.
	 */
	public SeaRoutePainter(MyMap map) {
		SeaRoutePainter.map = map;
		initCircleDiameter(map);

	}

	private static void initCircleDiameter(MyMap map) {
		// Init circle diameter array
		GeoPosition pos = new GeoPosition(0, 0);
		GeoPosition pcopy = new GeoPosition(pos.getLatitude() + Constants.POINT_DISTANCE_GEO,
				pos.getLongitude() + Constants.POINT_DISTANCE_GEO);

		for (int i = 1; i <= Constants.MAX_ZOOM_LEVEL; i++) {
			Point2D p = map.getTileFactory().geoToPixel(pos, i);
			circleDiameter[i] = (int) Math.abs(map.getTileFactory().geoToPixel(pcopy, i).getX() - p.getX());
			drawEdges.add(new ArrayList<>());
		}
	}

	public static List<SeaEdge> calcDrawEdges(List<NodeEdge> seaRoute) {
		initCircleDiameter(map);

		if (drawEdges.get(map.getZoom()-1).isEmpty()) {
			drawEdges.set(map.getZoom()-1,  calcSeaLines(seaRoute));
		}

		return drawEdges.get(map.getZoom()-1);
	}


	/**
	 * Adds the created {@link PointPath}-Element to the draw edges array as
	 * {@link SeaEdge}.
	 * 
	 * @param edge
	 * @param start
	 * @param dest
	 * @param path
	 */
	private static void addDrawEdge(Edge edge, GeoPosition start, GeoPosition dest, PointPath path) {
		SeaEdge seaEdge = new SeaEdge(start, dest);
		seaEdge.setPath(path.getPath());

		seaEdge.edgeIds.add(edge.id);
		drawEdges.get(map.getZoom()-1).add(seaEdge);

	}

	/**
	 * Calculates {@link Path2D}-Object for all used sea ways and returns them
	 * in a {@link List} of {@link SeaEdge}.
	 * 
	 * @return {@link List} of {@link SeaEdge} with calculated {@link Path2D}s,
	 *         or an empty list if the sea data consists of the wrong type.
	 */
	private static List<SeaEdge> calcSeaLines(List<NodeEdge> seaRoute) {
//		drawEdges.clear();

		for (int i = 0; i < seaRoute.size(); i++) {
			NodeEdge edge = seaRoute.get(i);

			GeoPosition lastPos = null;
			Point2D lastCircleP = null;
			// Add Start- and Endpoint
			if (edge.getPoints().size() > 3) { // min. 3 points needed for
												// quad function
				// Start
				lastPos = edge.getPosition(0);
				lastCircleP = getCirclePoint(convertGeo(edge.getStart()), convertGeo(lastPos));
				PointPath path = new PointPath();
				path.moveTo(convertGeo(edge.getStart()));
				path.lineTo(lastCircleP);
				addDrawEdge(edge, edge.getStart(), lastPos, path);
			} else {
				lastCircleP = convertGeo(edge.getStart());
				lastPos = edge.getPosition(0);
			}

			for (int j = 1; j < edge.getPoints().size(); j++) {
				GeoPosition pos = edge.getPosition(j);

				Point2D circlePIn = getCirclePoint(pos, lastPos);
				Point2D circlePOut = getCirclePoint(lastPos, pos);
				PointPath path = new PointPath();
				if (checkEquatorEdge(pos, lastPos)) {
					// init vars for orientation
					GeoPosition pos1 = pos;
					GeoPosition pos2 = lastPos;
					if (pos.getLongitude() < 90) {
						pos1 = lastPos;
						pos2 = pos;
					}

					// draw first part
					GeoPosition mirrorPoint = new GeoPosition(pos2.getLatitude(), (360 + pos2.getLongitude()));
					path.moveTo(convertGeo(pos1));
					path.lineTo(convertGeo(mirrorPoint));

					// draw second part
					mirrorPoint = new GeoPosition(pos1.getLatitude(), (-360 + pos1.getLongitude()));

					path.moveTo(convertGeo(pos2));
					path.lineTo(convertGeo(mirrorPoint));

					lastPos = pos;
					lastCircleP = convertGeo(pos);
					addDrawEdge(edge, pos1, mirrorPoint, path);
				} else {
					path.moveTo(lastCircleP);
					path.quadTo(convertGeo(lastPos), circlePIn);
					addDrawEdge(edge, convertPoint(lastCircleP), convertPoint(circlePIn), path);

					path = new PointPath();
					path.moveTo(circlePIn);
					path.lineTo(circlePOut);
					addDrawEdge(edge, convertPoint(circlePIn), convertPoint(circlePOut), path);

					lastPos = pos;
					lastCircleP = circlePOut;
				}

			}
			// End
			Point2D circleP = getCirclePoint(convertGeo(edge.getPosition(-2)), convertGeo(edge.getPosition(-1)));
			PointPath path = new PointPath();
			path.moveTo(getCirclePoint(convertGeo(edge.getPosition(-1)), convertGeo(edge.getPosition(-2))));
			path.lineTo(circleP);
			addDrawEdge(edge, edge.getPosition(-2), edge.getPosition(-1), path);
			
			path.moveTo(circleP);
			path.quadTo(convertGeo(edge.getPosition(-1)), getCirclePoint(edge.getDest(), edge.getPosition(-1)));
			path.lineTo(convertGeo(edge.getDest()));
			addDrawEdge(edge, edge.getPosition(-1), edge.getDest(), path);
		}
		return drawEdges.get(map.getZoom()-1);
	}

	/**
	 * Draws all possible sea ways on the saved map instance For debugging
	 */
	public static void drawPossibleSeaEdges(Graphics2D g) {
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.pink);
		List<Edge> route = MainController.getInstance().getSeaController().getEdges();

		// GeoPosition p1 = new GeoPosition(49.55372551347579, -22.587890625);

		for (Edge edge : route) {

			// // pos=[49.55372551347579, -22.587890625] -> 266057 300000km
			// if (edge.getStart().equals(p1)) {
			// System.out.println(edge.getGeoDistance() + " PING");
			// }
			//

			GeoPosition last = null;
			for (GeoPosition pos : edge.getPositions()) {
				if (last == null) {
					last = pos;
					continue;
				}
				if (checkEquatorEdge(last, pos)) {
					drawSimpleEquatorEdge(g, last, pos);
				} else {
					DefaultRoutePainter.drawNormalLine(g, map, last, pos);
					last = pos;
				}
			}
		}
		drawDebugNodeInfos(g);
	}

	/**
	 * see {@link #getCirclePoint(Point2D, Point2D)}
	 * 
	 * @param linePoint
	 * @param circlePoint
	 * @return crossing {@link Point2D} of line and circle.
	 */
	private static Point2D getCirclePoint(GeoPosition linePoint, GeoPosition circlePoint) {
		return getCirclePoint(convertGeo(linePoint), convertGeo(circlePoint));
	}

	/**
	 * Converts an {@link GeoPosition} to a {@link Point2D}
	 * 
	 * @param geopos
	 *            {@link GeoPosition} to convert
	 * @return the corresponding {@link Point2D} to the {@link GeoPosition}
	 */
	private static Point2D convertGeo(GeoPosition geopos) {
		return map.getTileFactory().geoToPixel(geopos, map.getZoom());
	}

	/**
	 * Converts the given {@link Point2D} to the corresponding
	 * {@link GeoPosition}
	 * 
	 * @param point
	 *            {@link Point2D} to convert
	 * @return the corresponding {@link GeoPosition} to the {@link Point2D}
	 */
	private static GeoPosition convertPoint(Point2D point) {
		return map.getTileFactory().pixelToGeo(point, map.getZoom());
	}

	private static Point2D getCirclePoint(Point2D p1, Point2D p2) {
		return getCirclePoint(p1, p2, circleDiameter[map.getZoom()] / 2);
	}

	public static Point2D getCirclePoint(GeoPosition p1, GeoPosition p2, double r) {
		return getCirclePoint(map.getPixelPos(p1), map.getPixelPos(p2), r);
	}
	
	/**
	 * Calculates the crossing point from the line between p1 and p2, and the
	 * circle which has p2 as center and the value of the member variable
	 * circleDiameter as diameter
	 * 
	 * @param p1
	 *            startpoint line
	 * @param p2
	 *            center of circle
	 * @param r
	 *            radius of the circle
	 * @return crossing {@link Point2D} of line and circle.
	 */
	public static Point2D getCirclePoint(Point2D p1, Point2D p2, double r) {
		// // gerade: y = mg * x + b | mg=y2-y1/x2-x1, b = y1

		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();

		double mg = (y2 - y1) / (x2 - x1);
		double b = -mg * x1 + y1;
		double b2 = -mg * x2 + y2;
		logger.debug("b: " + b + " - b2: " + b2);

		double m1 = p2.getX();
		double m2 = p2.getY();

		double p = (mg * b - m1 - (m2 * mg)) / (1 + (mg * mg));// p/2
		double q = ((m1 * m1) + (b * b) - 2 * m2 * b + (m2 * m2) - (r * r)) / (1 + (mg * mg));

		logger.debug("p: " + p + " - q: " + q);

		double diskriminante = (p * p) - q;
		logger.debug("diskriminante: " + diskriminante);

		double xres1 = -p + Math.sqrt(diskriminante);
		double xres2 = -p - Math.sqrt(diskriminante);

		double yres1 = mg * xres1 + b;
		double yres2 = mg * xres2 + b;

		logger.debug("P1: " + xres1 + " - " + yres1);
		logger.debug("P2: " + xres2 + " - " + yres2);
		logger.debug("PStar: " + x1 + " - " + y1);
		logger.debug("PDest: " + x2 + " - " + y2 + " | r:" + r);

		Point2D cp1 = new Point((int) xres1, (int) yres1);
		Point2D cp2 = new Point((int) xres2, (int) yres2);

		Point2D result = cp1;
		if (dist(p1, cp1) > dist(p1, cp2)) {
			result = cp2;
		}

		return result;
	}

	/**
	 * Calculates the distance between two {@link Point2D}s.
	 * 
	 * @return distance between the given points as double
	 */
	private static double dist(Point2D p1, Point2D p2) {
		double x = p1.getX() - p2.getX();
		double y = p1.getY() - p2.getY();
		return Math.sqrt((x * x) + (y * y));
	}

	private static void drawDebugNodeInfos(Graphics2D g) {
		g.setColor(Color.red);
		g.setFont(new Font("default", Font.BOLD, 16));
		for (Edge edge : MainController.getInstance().getSeaController().getEdges()) {
			GeoPosition pos = edge.getDest();
			Point2D p = convertGeo(pos);

			int diameter = circleDiameter[map.getZoom()];
			g.drawOval((int) (p.getX() - (diameter / 2)), (int) (p.getY() - (diameter / 2)), diameter, diameter);

			final int circle = 4;
			g.fillOval((int) (p.getX() - (circle / 2)), (int) (p.getY() - (circle / 2)), circle, circle);

			if (edge.getInfo() != null) {
				g.drawString(edge.getInfo(), (int) p.getX() + 15, (int) p.getY());
			}
		}

	}

	private static boolean checkEquatorEdge(GeoPosition point, GeoPosition last) {
		return ((last.getLongitude() > 90) && (point.getLongitude() < -30)
				|| (last.getLongitude() < -30) && (point.getLongitude() > 90));
	}

	private static Point2D[] drawSimpleEquatorEdge(Graphics2D g, GeoPosition from, GeoPosition to) {
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

	/**
	 * @param d
	 * @return
	 */
	public static double circleDiameterFaktor(double d) {
		return circleDiameter[map.getZoom()] * d;
	}

	/**
	 * @param pos
	 * @param pos2
	 * @param circleDiameterFaktor
	 * @return
	 */
	public static GeoPosition getCirclePointAsGeo(GeoPosition pos, GeoPosition pos2, double r) {
		return map.getGeoPos(getCirclePoint(pos, pos2, r));
	}

}
