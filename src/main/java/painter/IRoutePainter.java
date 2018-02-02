package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import gui.MyMap;
import main.Constants;
import main.RouteController;
import models.nodeBased.NodeEdge;
import models.pointBased.Edge;

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
	
	public static void drawUsedEdgesGreyLines(Graphics2D g, MyMap map, RouteController routeController) {
		// Manipulate the graphics object one time before the loop instead of
		// every run, because it is a resource consuming operation
		g.setColor(Color.GRAY);
		g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int i = 0; i < routeController.getPaintSeaRoute().size(); i++) {
			Edge edge = routeController.getPaintSeaRoute().get(i);
			if (edge instanceof NodeEdge) {
				drawGreyEdgeLine(g, map, (NodeEdge) edge);
			}
		}
		if (Constants.optimzeLandRoutes) {
			for (Edge edge : routeController.getPaintRoute()) {
				drawGreyEdgeLine(g, map, (NodeEdge) edge);
			}
		}
	}
	
	static void drawGreyEdgeLine(Graphics2D g, MyMap map, NodeEdge edge) {
		// Graphic manipulation has to be done before the loop
		// g.setColor(Color.DARK_GRAY);
		// g.setStroke(new BasicStroke(1.2f));
		g.draw(edge.getShape(map));
	}

}