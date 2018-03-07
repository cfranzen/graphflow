/**
 * 
 */
package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import gui.MyMap;
import main.Constants;
import main.RouteController;
import models.nodeBased.NodeEdge;
import models.pointBased.Edge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class PointPicturePainter implements IRoutePainter {

	private RouteController routeController;
	private Map<Integer, Color> colorMap = null;
	private boolean colorBlack;

	/**
	 * 
	 */
	public PointPicturePainter(boolean colorBlack) {
		this.colorBlack = colorBlack;
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
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#drawRoute(java.awt.Graphics2D, gui.MyMap)
	 */
	@Override
	public void drawRoute(Graphics2D g, MyMap map) {
		if (colorMap == null) {
			initColorMap();
		}
		nodeBasedColoring(g, map);
	}

	private void nodeBasedColoring(Graphics2D g, MyMap map) {
		List<Edge> route = routeController.getPaintRoute();
		route.addAll(routeController.getSeaRoute());
		g.setColor(Color.BLACK);
		for (int i = 0; i < route.size(); i++) {
			drawEdge((NodeEdge) route.get(i), map, g);
		}
	}

	private void drawEdge(Edge edge, MyMap map, Graphics2D g) {
		long commodities = LongStream.of(edge.getWorkloads()).sum();
		if (commodities == 0) {
			return;
		}

		double distance = map.getPixelPos(edge.getStart()).distanceSq(map.getPixelPos(edge.getDest()));

		float lineWidth = (float) (Constants.MAX_ZOOM_LEVEL - map.getZoom() + 1);
		float space = Math.max((float) ((distance - (commodities * lineWidth)) / (commodities + 1)) - lineWidth / 2, 0);

		Stroke dashed = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 5,
				new float[] { lineWidth, space }, space);

		g.setStroke(dashed);
		if (!colorBlack) {
			g.setColor(colorMap.get(edge.id));
		}
		DefaultRoutePainter.drawNormalLine(g, map, edge.getStart(), edge.getDest());
	}

	/**
	 * @param routeController
	 * 
	 */
	private void initColorMap() {
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

}
