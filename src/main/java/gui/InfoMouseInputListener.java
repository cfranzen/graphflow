/**
 * 
 */
package gui;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import main.Controller;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class InfoMouseInputListener extends MouseInputAdapter {

	private Controller controller;

	/**
	 * @param viewer
	 *            the jxmapviewer
	 */
	public InfoMouseInputListener(Controller controller) {
		this.controller = controller;
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
