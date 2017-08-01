package painter;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import gui.MyMap;
import main.RouteController;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public interface IRoutePainter extends Painter<JXMapViewer> {

	boolean antiAlias = false;

	/**
	 * @param route
	 *            the route to set
	 */
	void setRouteController(RouteController routeController);

	void setTimeStep(int time);

	default void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawRoute(g, (MyMap) map);
		g.dispose();
	}

	/**
	 * @param g
	 * @param map
	 */
	void drawRoute(Graphics2D g, MyMap map);

}