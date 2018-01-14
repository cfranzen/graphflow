package gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
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
import org.jxmapviewer.viewer.WaypointPainter;

import main.MainController;
import main.RouteController;
import models.CapacityWaypoint;
import models.Constants;
import newVersion.main.PaintController;
import newVersion.main.WaypointController;
import painter.CapacityWaypointRenderer;
import painter.DefaultRoutePainter;
import painter.IRoutePainter;

/**
 * 
 * Facade for the {@link JXMapViewer}-framework. The used map component is also
 * controller and gui component.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MyMap extends JXMapViewer implements PropertyChangeListener {

	private static final long serialVersionUID = -6620174270673711401L;

	private WaypointPainter<CapacityWaypoint> waypointPainter;

	private PaintController routePaintController;

	/**
	 * Default Constructor, initializes the tile factory.
	 */
	public MyMap(MainController controller, RouteController routeController, WaypointController waypointController) {
		super();

		addUserInteractions(controller, routeController, waypointController);
		setUpTileFactory();

		// Default values
		setAddressLocation(new GeoPosition(50.11, 8.68)); // Frankfurt
		setZoom(15);

		initPainters(routeController);

		setMinimumSize(new Dimension(250, 250));
		setPreferredSize(new Dimension(1400, 400));
	}

	/**
	 * Sets the given timestep in the {@link DefaultRoutePainter} and repaints
	 * the gui.
	 * 
	 * @param time
	 *            to set
	 */
	public void setTime(int time) {
		routePaintController.setTimeStep(time);
		updateMap();
	}

	public void updateMap() {
		invalidate();
		repaint();
	}

	public GeoPosition getCoordsForMouse(MouseEvent event) {
		return convertPointToGeoPosition(new Point(event.getX(), event.getY()));
	}

	public GeoPosition getGeoPos(double x, double y) {
		return getGeoPos(new Point((int) x, (int) y));
	}

	public GeoPosition getGeoPos(Point2D point) {
		return getTileFactory().pixelToGeo(point, getZoom());
	}

	public Point2D getPixelPos(GeoPosition pos) {
		return getTileFactory().geoToPixel(pos, getZoom());
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
	}

	/**
	 * Adds the {@link MouseListener}s for user-interaction
	 * 
	 * @param controller
	 * @param routeController
	 */
	private void addUserInteractions(MainController controller, RouteController routeController, WaypointController waypointController) {
		MouseInputListener mia = new PanMouseInputListener(this);
		addMouseListener(mia);
		addMouseMotionListener(mia);
		addMouseListener(new CenterMapListener(this));
		mia = new InfoMouseInputListener(this);
		addMouseListener(mia);
		addMouseMotionListener(mia);
		addMouseWheelListener(new ZoomMouseWheelListenerCenter(this));
		addMouseWheelListener(new WaypointMouseWheelListener(this, waypointController));
		addKeyListener(new PanKeyListener(this));
		addMouseListener(new WaypointClickMouseListener(this, routeController));
	}

	/**
	 * Creates a {@link TileFactoryInfo} for OpenStreetMap and sets up the local
	 * file cache
	 */
	private void setUpTileFactory() {
		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(4);
		setTileFactory(tileFactory);

		// Setup local file cache
		String baseURL = info.getBaseURL();
		File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
		LocalResponseCache.installResponseCache(baseURL, cacheDir, false);
	}

	/**
	 * Initializes the {@link IRoutePainter}-Objects of the graph.
	 */
	private void initPainters(RouteController routeController) {

		routePaintController = PaintController.getInstance();
		routePaintController.setRouteController(routeController);

		// Create a waypoint painter that takes all the waypoints
//		waypointPainter = new SwingWaypointOverlayPainter();
		waypointPainter = new WaypointPainter<>();
		waypointPainter.setRenderer(new CapacityWaypointRenderer());

		// Create a compound painter that uses both the route-painter and the
		// waypoint-painter
		List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(routePaintController);
		painters.add(waypointPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		setOverlayPainter(painter);
	}

	/**
	 * @param waypoints
	 */
	public void setWaypoints(Set<CapacityWaypoint> waypoints) {
		waypointPainter.setWaypoints(waypoints);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if (Constants.EVENT_NAME_WAYPOINT_FROM.equals(propertyName)
				|| Constants.EVENT_NAME_WAYPOINT_TO.equals(propertyName)
				|| Constants.EVENT_NAME_EDGE_CHANGE.equals(propertyName)) {
			repaint();
		}
	}

	/**
	 * @return
	 */
	public Set<CapacityWaypoint> getWaypoints() {
		return waypointPainter.getWaypoints();
	}

}
