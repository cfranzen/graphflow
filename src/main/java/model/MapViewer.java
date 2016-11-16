package model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Track;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.LayerRenderer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * A wrapper for the actual {@link JXMapViewer} component. It connects to the 
 * typical application classes. 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapViewer extends JComponent implements LayerRenderer { 
 
    private static final long serialVersionUID = -1636285199192286728L; 
 
    private CompoundPainter<JXMapViewer> painter; 
    private JXMapViewer mapViewer = new JXMapViewer(); 
 
    
    /**
     * Constructs a new instance 
     */ 
    public MapViewer() {
    	
        // Create a TileFactoryInfo for OpenStreetMap 
        TileFactoryInfo info = new OSMTileFactoryInfo(); 
        DefaultTileFactory tileFactory = new DefaultTileFactory(info); 
        tileFactory.setThreadPoolSize(8); 
        mapViewer.setTileFactory(tileFactory); 
 
        // Setup local file cache 
        String baseURL = info.getBaseURL(); 
        File cacheDir = new File(System.getProperty("user.home") 
                + File.separator + ".jxmapviewer2"); 
        LocalResponseCache.installResponseCache(baseURL, cacheDir, false); 
 
        // Add interactions 
        MouseInputListener mia = new PanMouseInputListener(mapViewer); 
        mapViewer.addMouseListener(mia); 
        mapViewer.addMouseMotionListener(mia); 
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer)); 
 
        painter = new CompoundPainter<>(); 
        mapViewer.setOverlayPainter(painter); 
        GeoPosition frankfurt = new GeoPosition(50, 7, 0, 8, 41, 0); 
        
        // Set the focus 
        mapViewer.setZoom(10); 
        mapViewer.setAddressLocation(frankfurt); 
 
        setLayout(new BorderLayout()); 
        add(mapViewer, BorderLayout.CENTER); 
    } 
 
    public void moveTo(double longitude, double latitude) {
    	mapViewer.setAddressLocation(new GeoPosition(latitude, longitude));
    }

    @Override
    public synchronized MouseWheelListener[] getMouseWheelListeners() {
    	return mapViewer.getMouseWheelListeners();
    }
    
	/* (non-Javadoc)
	 * @see org.graphstream.ui.swingViewer.LayerRenderer#render(java.awt.Graphics2D, org.graphstream.ui.graphicGraph.GraphicGraph, double, int, int, double, double, double, double)
	 */
	@Override
	public void render(Graphics2D graphics, GraphicGraph graph, double px2Gu, int widthPx, int heightPx, double minXGu,
			double minYGu, double maxXGu, double maxYGu) {
		
		mapViewer.paintAll(graphics);
	}
    
}