package gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import main.Controller;
import models.Edge;

/**
 * 
 * Facade for the {@link JXMapViewer}-framework
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Map extends JXMapViewer {

	private static final long serialVersionUID = -6620174270673711401L;

	private Controller controller;

	private Set<Waypoint> waypoints = new HashSet<>();;
	private WaypointPainter<Waypoint> waypointPainter;
	private RoutePainter routePainter = new RoutePainter(Collections.emptyList());
	private List<Edge> route = new ArrayList<>();

	/**
	 * Default Constructor, initializes the tile factory.
	 */
	public Map(Controller controller) {
		super();
		this.controller = controller;

		addUserInteractions();
		setUpTileFactory();

		// Default values
		setAddressLocation(new GeoPosition(50.11, 8.68)); // Frankfurt
		setZoom(3);

		initPainters();
		// Point2D pixelPoint = getTileFactory().geoToPixel(geoPos, getZoom());

		setMinimumSize(new Dimension(250, 250));
		setPreferredSize(new Dimension(1400, 400));
	}

	private void initPainters() {
		// Create a waypoint painter that takes all the waypoints
		waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setRenderer(new CapacityWaypointRenderer());
		waypointPainter.setWaypoints(waypoints);

		// Create a compound painter that uses both the route-painter and the
		// waypoint-painter
		List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(routePainter);
		painters.add(waypointPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		setOverlayPainter(painter);
	}

	/**
	 * Adds the given nodes to the graph.
	 * 
	 * @param nodes
	 *            {@link List} with {@link GeoPosition}s as nodes
	 */
	public void addPositions(List<GeoPosition> nodes) {
		for (GeoPosition geoPos : nodes) {
			waypoints.add(new CapacityWaypoint(geoPos.getLatitude(), geoPos.getLongitude(), 0));
		}

		zoomToBestFit(new HashSet<>(nodes), 0.7);
		waypointPainter.setWaypoints(waypoints);
	}

	/**
	 * Adds the given {@link Edge}s to the graph.
	 * 
	 * @param edges
	 *            {@link List} with {@link Edge}s.
	 */
	public void addEdges(List<Edge> edges) {
		route.addAll(edges);
		routePainter.setRoute(route);
	}

	public void setTime(int time) {
		routePainter.setTimeStep(time);
		repaint();
	}

	/**
	 * Adds the {@link MouseListener}s
	 */
	private void addUserInteractions() {
		MouseInputListener mia = new PanMouseInputListener(this);
		addMouseListener(mia);
		addMouseMotionListener(mia);
		addMouseListener(new CenterMapListener(this));
		mia = new InfoMouseInputListener(controller);
		addMouseListener(mia);
		addMouseMotionListener(mia);
		addMouseWheelListener(new ZoomMouseWheelListenerCenter(this));
		addKeyListener(new PanKeyListener(this));
	}

	/**
	 * Creates a {@link TileFactoryInfo} for OpenStreetMap and sets up the local
	 * file cache
	 */
	private void setUpTileFactory() {
		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);
		setTileFactory(tileFactory);

		// Setup local file cache
		String baseURL = info.getBaseURL();
		File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
		LocalResponseCache.installResponseCache(baseURL, cacheDir, false);
	}

	/**
	 * This method is called automatically when the mouse is over the component.
	 * Based on the location of the event, we detect if we are over one of the
	 * circles. If so, we display some information relative to that circle If
	 * the mouse is not over any circle we return the tooltip of the component.
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		Point p = new Point(event.getX(), event.getY());
		for (Waypoint waypoint : waypoints) {
			if (isMouseOnWaypoint(p, waypoint)) {
				return getTooltipForWaypoint(waypoint);
			}
		}
		return null;
	}

	/**
	 * @param waypoint
	 * @return
	 */
	private String getTooltipForWaypoint(Waypoint waypoint) {
		// TODO Change to Node object
		return "TOOLTIP";
	}

	/**
	 * @param p
	 * @param waypoint
	 * @return
	 */
	private boolean isMouseOnWaypoint(Point p, Waypoint waypoint) {
		double DELTA = 10;

		Point2D nodePos = convertGeoPositionToPoint(waypoint.getPosition());
		double deltaX = Math.abs(p.x - nodePos.getX());
		double deltaY = Math.abs(p.y - nodePos.getY());

		return (deltaX < DELTA) && (deltaY < DELTA);
	}

}
