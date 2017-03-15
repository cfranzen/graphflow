package gui;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import org.jxmapviewer.viewer.GeoPosition;

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
			System.out.println(controller.getMapViewer().getCoordsForMouse(e).toString());
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
		super.mouseMoved(e);
	}

}
