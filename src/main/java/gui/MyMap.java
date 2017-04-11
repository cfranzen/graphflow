package gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
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

import main.MainController;
import models.CapacityWaypoint;
import painter.CapacityWaypointRenderer;
import painter.DefaultRoutePainter;
import painter.IRoutePainter;
import painter.SeaRoutePainter;

/**
 * 
 * Facade for the {@link JXMapViewer}-framework. The used map component is also
 * controller and gui component.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MyMap extends JXMapViewer {

	private static final long serialVersionUID = -6620174270673711401L;

	private MainController controller;

	private Set<Waypoint> waypoints = new HashSet<>();;
	private WaypointPainter<Waypoint> waypointPainter;

	private IRoutePainter routePainter = new DefaultRoutePainter();

	// TODO Refactor
	private IRoutePainter defaultPainter = new DefaultRoutePainter();
	private IRoutePainter seaRoutePainter;

	/**
	 * Default Constructor, initializes the tile factory.
	 */
	public MyMap(MainController controller) {
		super();
		this.controller = controller;

		addUserInteractions();
		setUpTileFactory();

		// Default values
		setAddressLocation(new GeoPosition(50.11, 8.68)); // Frankfurt
		setZoom(3);

		this.seaRoutePainter = new SeaRoutePainter(this);
		initPainters();

		
		setMinimumSize(new Dimension(250, 250));
		setPreferredSize(new Dimension(1400, 400));
	}

	/**
	 * @return the waypoints
	 */
	public List<GeoPosition> getWaypoints() {
		List<GeoPosition> result = new ArrayList<>(waypoints.size());
		for (Waypoint waypoint : waypoints) {
			result.add(waypoint.getPosition());
		}
		return result;
	}

	/**
	 * Adds the given nodes to the graph.
	 * 
	 * @param nodes
	 *            {@link List} with {@link GeoPosition}s as nodes
	 */
	public void addPositions(List<GeoPosition> nodes) {
		List<GeoPosition> zoomNodes = new ArrayList<>();
		if (MainController.onlyGermany) {
			for (GeoPosition geoPos : nodes) {
				double lat = geoPos.getLatitude();
				double lon = geoPos.getLongitude();

				// Only Germany
				if ((6 < lon && lon < 14) && (45 < lat && lat < 55)) {
					waypoints.add(new CapacityWaypoint(lat, lon, 0));
					zoomNodes.add(geoPos);
				}
			}
		} else {
			for (GeoPosition geoPos : nodes) {
				waypoints.add(new CapacityWaypoint(geoPos.getLatitude(), geoPos.getLongitude(), 0));
				zoomNodes.add(geoPos);
			}
		}
		// TODO schöner machen
		zoomToBestFit(new HashSet<>(zoomNodes), 0.7);
		waypointPainter.setWaypoints(waypoints);
	}

	/**
	 * Sets the given timestep in the {@link DefaultRoutePainter} and repaints
	 * the gui.
	 * 
	 * @param time
	 *            to set
	 */
	public void setTime(int time) {
		routePainter.setTimeStep(time);
		repaint();
	}

	public GeoPosition getCoordsForMouse(MouseEvent event) {
		return convertPointToGeoPosition(new Point(event.getX(), event.getY()));
	}

	/**
	 * This method is called automatically when the mouse is over the component.
	 * Based on the location of the event, we detect if we are over one of the
	 * circles. If so, we display some information relative to that circle If
	 * the mouse is not over any circle we return the tooltip of the component.
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		GeoPosition position = getCoordsForMouse(event);
		String text = String.format("Latitude: %f\nLongitude: %f", position.getLatitude(), position.getLongitude());

		return "<html><p width=\"250\">" + text + "</p></html>";
		// Point p = new Point(event.getX(), event.getY());
		// for (Waypoint waypoint : waypoints) {
		// if (isMouseOnWaypoint(p, waypoint)) {
		// return getTooltipForWaypoint(waypoint);
		// }
		// }
		// return null;
	}

	/**
	 * Adds the {@link MouseListener}s for user-interaction
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
	 * Initializes the {@link DefaultRoutePainter} and {@link WaypointPainter}
	 * of the graph.
	 */
	private void initPainters() {

		// Create a waypoint painter that takes all the waypoints
		// TODO Refactor, do not use jmapviewers waypoint class
		waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setRenderer(new CapacityWaypointRenderer());
		waypointPainter.setWaypoints(waypoints);

		// Create a compound painter that uses both the route-painter and the
		// waypoint-painter
		List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(getRoutePainter(this.getZoom()));
		painters.add(seaRoutePainter);
		painters.add(waypointPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		setOverlayPainter(painter);
	}

	public IRoutePainter getRoutePainter(int zoomlevel) {
		switch (zoomlevel) {
		case 1:
		default:
			return defaultPainter;
		}
	}

	/**
	 * @param waypoints
	 */
	public void setWaypoints(Set<Waypoint> waypoints) {
		this.waypoints = waypoints;
		waypointPainter.setWaypoints(waypoints);

	}

}
