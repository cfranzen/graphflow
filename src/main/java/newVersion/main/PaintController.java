/**
 * 
 */
package newVersion.main;

import java.awt.Graphics2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import main.RouteController;
import newVersion.painter.NewEntityFlowPainter;
import painter.EntityFlowPainter;
import painter.IRoutePainter;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class PaintController implements Painter<JXMapViewer> {

	private static PaintController instance;
	private IRoutePainter painter1 = new EntityFlowPainter();
	private NewEntityFlowPainter painter2 = new NewEntityFlowPainter();
	public static boolean flagNew = false; // TODO refactor
	
	private PaintController() {
		// NOOP
	}
	
	public static PaintController getInstance() {
		if (instance == null) {
			instance = new PaintController();
		}
		return instance;
	}
	
	
	/* (non-Javadoc)
	 * @see org.jxmapviewer.painter.Painter#paint(java.awt.Graphics2D, java.lang.Object, int, int)
	 */
	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		if (flagNew) {
			painter2.paint(g, map, w, h);
		} else {
			painter1.paint(g, map, w, h);
		}
	}
	
	public void setRouteController(RouteController routeController) {
		painter1.setRouteController(routeController);
		painter2.setRouteController(routeController);
	}

	/**
	 * @param time
	 */
	public void setTimeStep(int time) {
		painter1.setTimeStep(time);
		painter2.setTimeStep(time);
	}

}
