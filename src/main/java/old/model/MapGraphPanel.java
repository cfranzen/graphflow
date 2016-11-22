/**
 * 
 */
package old.model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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

import old.examples.GraphstreamGraph;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapGraphPanel extends JComponent  {

	private static final long serialVersionUID = 1344779740268171587L;

	public MapViewer mapViewer = new MapViewer();
	private DynamicGraph graph = new DynamicGraph();

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


		Set<GeoPosition> positions = new HashSet<>();
		for (Node node : graph.getNodeSet()) {
			Double[] d = getNodeCoords(node);
			positions.add(new GeoPosition(d[0], d[1]));
		}

		mapViewer.zoomToBestFit(positions, 0.8);
//		mapViewer.setSize(400, 400);
		
		setLayout(new BorderLayout());
		DefaultView view = graph.getView();
		view.setBackLayerRenderer(mapViewer);
		add(mapViewer, BorderLayout.CENTER);
//		add((Component) view, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/**
	 * @param positions
	 */

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



}
