/**
 * 
 */
package old.model;

import java.awt.Container;
import java.awt.Graphics2D;

import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.LayerRenderer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;

/**
 * Facade for the {@link JXMapViewer}-framework
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapViewer extends JXMapViewer implements LayerRenderer{

	private static final long serialVersionUID = -2418754694351156509L;

	/**
	 * Constructor
	 */
	public MapViewer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#isShowing()
	 */
	@Override
	public boolean isShowing() {
		if (isVisible()) {
			Container parent = this.getParent();
			return (parent == null) || parent.isShowing();
		}
		
		return false;
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
		paintAll(graphics);
		paintComponents(graphics);
	}
}
