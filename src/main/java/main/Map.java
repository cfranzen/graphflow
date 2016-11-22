/**
 * 
 */
package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.event.ListSelectionEvent;
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

import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;

/**
 * 
 * Facade for the {@link JXMapViewer}-framework
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Map extends JXMapViewer {

	private static final long serialVersionUID = -6620174270673711401L;

	private Set<Waypoint> waypoints;
	private WaypointPainter<Waypoint> waypointPainter;
	private RoutePainter routePainter = new RoutePainter(Collections.emptyList());
	private Collection<Edge> route = new ArrayList<>();

	/**
	 * Default Constructor, initializes the tile factory.
	 */
	public Map() {
		super();
		addUserInteractions();
		setUpTileFactory();
		setZoom(7);
		setAddressLocation(new GeoPosition(50.11, 8.68)); // Frankfurt
		waypoints = new HashSet<>();

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

	public void addPositions(Collection<Node> positions) {
		Set<GeoPosition> geoPosis = new HashSet<>();
		for (Node node : positions) {
			waypoints.add(new CapacityWaypoint(node.getLatitude(), node.getLongitude(), 0));
			geoPosis.add(new GeoPosition(node.getLatitude(), node.getLongitude()));
		}
		zoomToBestFit(geoPosis, 0.7);
		waypointPainter.setWaypoints(waypoints);

		routePainter.setRoute(route);
	}

	/**
	 * @param edges
	 */
	public void addEdges(SortedSet<Service> edges) {
		for (Service service : edges) {
			Node start = service.getStartNode();
			Node end = service.getEndNode();
			
			route.add(new Edge(new GeoPosition(start.getLatitude(),start.getLongitude()),
					new GeoPosition(end.getLatitude(), end.getLongitude()),
					service.getCapacity(ALLBITS), 0)); // TODO capacity and workload
		}
	}

	private void addUserInteractions() {
		MouseInputListener mia = new PanMouseInputListener(this);
		addMouseListener(mia);
		addMouseMotionListener(mia);
		addMouseListener(new CenterMapListener(this));
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

}
