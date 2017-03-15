/**
 * 
 */
package gui;

import java.awt.Graphics2D;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import models.Edge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public interface IRoutePainter extends Painter<JXMapViewer> {

	/**
	 * @param route
	 *            the route to set
	 */
//	void setRoute(List<Edge> route);

	void setTimeStep(int time);

	void paint(Graphics2D g, JXMapViewer map, int w, int h);

}