package gui;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import main.MainController;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class InfoMouseInputListener extends MouseInputAdapter {

	private MainController controller;

	private boolean showTooltip = false;

	/**
	 * @param viewer
	 *            the jxmapviewer
	 */
	public InfoMouseInputListener(MainController controller) {
		this.controller = controller;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			showTooltip = !showTooltip;
			controller.getSeaController().addNewNode(controller.getMapViewer().getCoordsForMouse(e));
		}
		if (e.getButton() == MouseEvent.BUTTON2) {
			controller.getSeaController().printNodes();
		}
		super.mouseClicked(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		String tooltip = showTooltip ? controller.getMapViewer().getToolTipText(e) : null;
		controller.getMapViewer().setToolTipText(tooltip);
//		System.out.println(controller.getMapViewer().getCoordsForMouse(e).toString() + " = x:" + e.getX() + " | y:" + e.getY());
		super.mouseMoved(e);
	}

}
