/**
 * 
 */
package main;

import java.awt.Graphics2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import gui.MyMap;
import painter.DefaultRoutePainter;
import painter.EntityFlowPainter;
import painter.IRoutePainter;
import painter.LinePicturePainter;
import painter.NewEntityFlowPainter;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class PaintController implements Painter<JXMapViewer> {

	private static final int MAX_PAINTERS = 5;
	private static PaintController instance;
	// private IRoutePainter painter1 = new EntityFlowPainter();
	// private IRoutePainter painter2 = new NewEntityFlowPainter();
	private IRoutePainter[] painters = new IRoutePainter[MAX_PAINTERS];
	private RouteController routeController;
	private int currentPainterIndex = 0;
	// private static boolean useNewPainter = false;

	private PaintController() {
		painters[0] = new DefaultRoutePainter();
		painters[1] = new EntityFlowPainter();
		painters[2] = new NewEntityFlowPainter();
		painters[3] = new LinePicturePainter(true);
		painters[4] = new LinePicturePainter(false);

		// NOOP
	}

	public static PaintController getInstance() {
		if (instance == null) {
			instance = new PaintController();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jxmapviewer.painter.Painter#paint(java.awt.Graphics2D,
	 * java.lang.Object, int, int)
	 */
	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		routeController.updatePaintRoute((MyMap) map);
		painters[currentPainterIndex].paint(g, map, w, h);
		// if (useNewPainter) {
		// painter2.paint(g, map, w, h);
		// } else {
		// painter1.paint(g, map, w, h);
		// }
	}

	public void setRouteController(RouteController routeController) {
		this.routeController = routeController;
		for (int i = 0; i < painters.length; i++) {
			painters[i].setRouteController(routeController);
		}
		// painter1.setRouteController(routeController);
		// painter2.setRouteController(routeController);
	}

	/**
	 * @param time
	 */
	public void setTimeStep(int time) {
		for (int i = 0; i < painters.length; i++) {
			painters[i].setTimeStep(time);
		}
		// painter1.setTimeStep(time);
		// painter2.setTimeStep(time);
	}

	public void useNewPainter() {
		currentPainterIndex = 2;
		// useNewPainter = true;
		MainController.getInstance().getMapViewer().repaint();
	}

	/**
	 * Increases or decreases the index for the next painter.
	 * 
	 * @param next <code>true</code>
	 *            increases the painter index</br>
	 *            <code>false</code> decreases the painter index
	 * 
	 */
	public void nextPainter(boolean next) {
		if (next) {
			currentPainterIndex = currentPainterIndex == MAX_PAINTERS - 1 ? 0 : ++currentPainterIndex;
		} else {
			currentPainterIndex = currentPainterIndex == 0 ? MAX_PAINTERS - 1 : --currentPainterIndex;
		}
		MainController.getInstance().getMapViewer().repaint();
	}

}
