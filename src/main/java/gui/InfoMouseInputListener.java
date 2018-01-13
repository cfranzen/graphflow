package gui;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class InfoMouseInputListener extends MouseInputAdapter {

	private MyMap map;

	private boolean showTooltip = false;

	public InfoMouseInputListener(MyMap map) {
		this.map = map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			showTooltip = !showTooltip;
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
		String tooltip = showTooltip ? map.getToolTipText(e) : null;
		map.setToolTipText(tooltip);
		super.mouseMoved(e);
	}

}
