/**
 * 
 */
package model;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.LayerRenderer;
import org.graphstream.ui.view.Viewer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapGraphPanel extends JComponent implements LayerRenderer {

	private static final long serialVersionUID = 1344779740268171587L;

	public JXMapViewer mapViewer = new JXMapViewer();
	private GraphstreamGraph graph = new DynamicGraph();

	/**
	 * Constructor
	 */
	public MapGraphPanel() {
		initMap();
	}

	/**
	 * Initalizes the {@link JXMapViewer} component
	 */
	private void initMap() {
		setUpTileFactory();

		// Add interactions
		MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);
		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

		// Set the focus
		mapViewer.setZoom(10); // TODO berechnen
		// GeoPosition frankfurt = new GeoPosition(50, 7, 0, 8, 41, 0);
		// mapViewer.setAddressLocation(frankfurt);

		Viewer viewer = new Viewer(graph.getGraphComponent(), Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.addView(Viewer.DEFAULT_VIEW_ID, Viewer.newGraphRenderer(), false);
		viewer.disableAutoLayout();

		// Layouting
		DefaultView defaultView = (DefaultView) (viewer.getDefaultView());
		defaultView.setBackLayerRenderer(this);

		setLayout(new BorderLayout());
		// add(mapViewer, BorderLayout.CENTER);
		add(viewer.getDefaultView());

		Set<GeoPosition> positions = new HashSet<>();
		for (Node node : graph.getGraphComponent().getNodeSet()) {
			Double[] d = getNodeCoords(node);
			positions.add(new GeoPosition(d[0], d[1]));
		}

		mapViewer.zoomToBestFit(positions, 0.8);
		mapViewer.setCenterPosition(calculateMapCenter(positions));

		mapViewer.revalidate();
	}

	/**
	 * @param positions
	 */
	private GeoPosition calculateMapCenter(Set<GeoPosition> positions) {
		double longitude = 0;
		double latitude = 0;
		for (GeoPosition geoPosition : positions) {
			longitude = Math.sqrt((longitude * longitude) + (geoPosition.getLongitude() * geoPosition.getLongitude()));
			latitude = Math.sqrt((latitude * latitude) + (geoPosition.getLatitude() * geoPosition.getLatitude()));
		}

		return new GeoPosition(latitude, longitude);
	}

	private Double[] getNodeCoords(Node node) {
		Object[] d = node.getAttribute("xy");
		return new Double[] { (double) d[0], (double) d[1] };
	}

	/**
	 * Creates a {@link TileFactoryInfo} for OpenStreetMap and sets up the local
	 * file cache
	 * 
	 * @return {@link TileFactoryInfo}
	 */
	public void setUpTileFactory() {
		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);
		mapViewer.setTileFactory(tileFactory);

		// Setup local file cache
		String baseURL = info.getBaseURL();
		File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
		LocalResponseCache.installResponseCache(baseURL, cacheDir, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.ui.swingViewer.LayerRenderer#render(java.awt.Graphics2D,
	 * org.graphstream.ui.graphicGraph.GraphicGraph, double, int, int, double,
	 * double, double, double)
	 */
	@Override
	public void render(Graphics2D graphics, GraphicGraph graph, double px2Gu, int widthPx, int heightPx, double minXGu,
			double minYGu, double maxXGu, double maxYGu) {
		mapViewer.paintAll(graphics);
	}

}
