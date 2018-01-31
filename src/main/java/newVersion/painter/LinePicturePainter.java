/**
 * 
 */
package newVersion.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gui.MyMap;
import main.RouteController;
import models.Constants;
import models.Edge;
import newVersion.models.MapNode;
import newVersion.models.NodeEdge;
import painter.DefaultRoutePainter;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class LinePicturePainter implements IRoutePainter {

	private RouteController routeController;
	private Map<Integer, Color> colorMap = null;
	private boolean nodeBasedColoring = true;

	/**
	 * 
	 */
	public LinePicturePainter(boolean nodeBasedColoring) {
		this.nodeBasedColoring = nodeBasedColoring;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#setRouteController(main.RouteController)
	 */
	@Override
	public void setRouteController(RouteController routeController) {
		this.routeController = routeController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#setTimeStep(int)
	 */
	@Override
	public void setTimeStep(int time) {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#drawRoute(java.awt.Graphics2D, gui.MyMap)
	 */
	@Override
	public void drawRoute(Graphics2D g, MyMap map) {
		initColorMap();
		// IRoutePainter.drawUsedEdgesGreyLines(g, map, routeController);
		if (nodeBasedColoring) {
			nodeBasedColoring(g, map);
		} else {
			directLineColoring(g, map);
		}
	}

	/**
	 * @param g
	 * @param map
	 */
	private void directLineColoring(Graphics2D g, MyMap map) {
		List<Edge> route = routeController.getPaintRoute();
		// set the stroke of the copy, not the original
		// for (Edge edge : route) {
		for (int i = 0; i < route.size(); i++) {
			Edge edge = route.get(i);
			Stroke dashed = new BasicStroke((int) (1.2 * Constants.MAX_ZOOM_LEVEL - map.getZoom() + 1),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
					new float[] { (float) (1.2 * Constants.MAX_ZOOM_LEVEL - map.getZoom() + 1), 20 }, 0);
			g.setStroke(dashed);
			g.setColor(colorMap.get(edge.id));
			DefaultRoutePainter.drawNormalLine(g, map, edge.getStart(), edge.getDest());
		}
		
		route = routeController.getPaintSeaRoute();
		for (int i = 0; i < route.size(); i++) {
			Edge edge = route.get(i);
			Stroke dashed = new BasicStroke((int) (1.2 * Constants.MAX_ZOOM_LEVEL - map.getZoom() + 1),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
					new float[] { (float) (1.2 * Constants.MAX_ZOOM_LEVEL - map.getZoom() + 1), 20 }, 0);
			g.setStroke(dashed);
			g.setColor(colorMap.get(edge.id));
			DefaultRoutePainter.drawNormalLine(g, map, edge.getStart(), edge.getDest());
		}
	}

	private void nodeBasedColoring(Graphics2D g, MyMap map) {
		List<Edge> route = routeController.getPaintRoute();
		for (int i = 0; i < route.size(); i++) {
			NodeEdge edge = (NodeEdge) route.get(i);
			g.setColor(colorMap.get(edge.id));
			for (int j = i * 4; j < edge.nodes.size(); j += 200) {
				MapNode node = edge.nodes.get(j);
				markSpot(g, map.getPixelPos(node.getPosition()), Constants.MAX_ZOOM_LEVEL - map.getZoom() + 1);
			}
		}
		route = routeController.getSeaRoute();
		for (int i = 0; i < route.size(); i++) {
			NodeEdge edge = (NodeEdge) route.get(i);
			g.setColor(colorMap.get(edge.id));
			for (int j = i * 4; j < edge.nodes.size(); j += 200) {
				MapNode node = edge.nodes.get(j);
				markSpot(g, map.getPixelPos(node.getPosition()), Constants.MAX_ZOOM_LEVEL - map.getZoom() + 1);
			}
		}
	}

	/**
	 * @param routeController
	 * 
	 */
	private void initColorMap() {
		if (colorMap != null) {
			return;
		}
		colorMap = new HashMap<>();
		for (Edge edge : routeController.getRoute()) {
			Color randomColor = new Color((int) (Math.random() * 0x1000000));
			randomColor.brighter();
			colorMap.put(edge.id, randomColor);
		}
		for (Edge edge : routeController.getSeaRoute()) {
			colorMap.put(edge.id, new Color((int) (Math.random() * 0x1000000)));
		}

	}

	private void markSpot(Graphics2D g, Point2D p, int zoomFaktor) {
		g.fillOval((int) p.getX(), (int) p.getY(), 4 * zoomFaktor, 4 * zoomFaktor);
	}

}
