/**
 * 
 */
package gui;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import main.Controller;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class InfoMouseInputListener extends MouseInputAdapter {

	private Point prev;
	private Controller controller;
	private Cursor priorCursor;

	/**
	 * @param viewer
	 *            the jxmapviewer
	 */
	public InfoMouseInputListener(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		if (SwingUtilities.isLeftMouseButton(evt)) {
			return;
		}

		prev = evt.getPoint();
		priorCursor = controller.getMapViewer().getCursor();
//		controller.getMapViewer().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		String tooltip = controller.getMapViewer().getToolTipText(e);
		controller.getMapViewer().setToolTipText(tooltip);
		super.mouseMoved(e);
	}

	
}
