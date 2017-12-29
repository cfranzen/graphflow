/**
 * 
 */
package gui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;

import newVersion.main.WaypointController;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class WaypointMouseWheelListener implements MouseWheelListener {

	private MyMap map;
	private WaypointController waypointController;

	/**
	 * 
	 */
	public WaypointMouseWheelListener(MyMap map, WaypointController waypointController) {
		this.map = map;
		this.waypointController = waypointController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
	 * MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		map.setWaypoints(new HashSet<>(waypointController.getWaypoints(map.getZoom())));
	}

}
