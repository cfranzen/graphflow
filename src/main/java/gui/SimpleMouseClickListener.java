package gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Use this interface if you only need to implement the click method.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public interface SimpleMouseClickListener extends MouseListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	default public void mousePressed(MouseEvent e) {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	default public void mouseReleased(MouseEvent e) {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	default public void mouseEntered(MouseEvent e) {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	default public void mouseExited(MouseEvent e) {
		// NOOP
	}

}
