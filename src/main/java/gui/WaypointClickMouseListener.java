/**
 * 
 */
package gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jxmapviewer.beans.AbstractBean;
import org.jxmapviewer.viewer.GeoPosition;

import main.MainController;
import main.RouteController;
import models.CapacityWaypoint;
import models.Constants;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class WaypointClickMouseListener extends AbstractBean implements MouseListener {

	private MyMap map;
	private CapacityWaypoint waypointFrom;
	private CapacityWaypoint waypointTo;

	/**
	 * @param myMap
	 */
	public WaypointClickMouseListener(MyMap myMap, RouteController routeController) {
		map = myMap;
		addPropertyChangeListener(routeController);
		addPropertyChangeListener(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		GeoPosition gp = map.getCoordsForMouse(e);
		// System.out.println("ClickPos:" + gp.toString() + " | Zoom: " +
		// map.getZoom());
		Map<CapacityWaypoint, Double> distanceMap = new HashMap<>();
		double precision = 0.25 * map.getZoom() / 3;
		for (CapacityWaypoint waypoint : map.getWaypoints()) {
			GeoPosition wp = waypoint.getPosition();
			double distance = MainController.getDistance(gp, wp);
			if (distance < precision) {
				distanceMap.put(waypoint, distance);
				// System.out.println(wp.toString() + " - DIST: " +
				// MainController.getDistance(gp, wp));

			}
		}
		Entry<CapacityWaypoint, Double> minEntry = null;
		for (Entry<CapacityWaypoint, Double> entry : distanceMap.entrySet()) {
			if (minEntry == null || minEntry.getValue() > entry.getValue()) {
				minEntry = entry;
			}
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			CapacityWaypoint oldValue = getWaypointFrom();
	
			if (minEntry != null) {
				// JOptionPane.showMessageDialog(map, "Nearest node:\n" +
				// minEntry.getKey().toString());
				this.waypointFrom = minEntry.getKey();
			} else {
				this.waypointFrom = null;
			}
			if (oldValue != null) {
				oldValue.setColorFlag(oldValue.getColorFlag() - 1);
			}
			if (getWaypointFrom() != null) {
				getWaypointFrom().setColorFlag(getWaypointFrom().getColorFlag() + 1);
			}
			firePropertyChange(Constants.EVENT_NAME_WAYPOINT_FROM, oldValue, getWaypointFrom());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			CapacityWaypoint oldValue = getWaypointTo();
			
			if (minEntry != null) {
				this.waypointTo = minEntry.getKey();
			} else {
				this.waypointTo = null;
			}
			if (oldValue != null) {
				oldValue.setColorFlag(oldValue.getColorFlag() - 2);
			}
			if (getWaypointTo() != null) {
				getWaypointTo().setColorFlag(getWaypointTo().getColorFlag() + 2);
			}
			firePropertyChange(Constants.EVENT_NAME_WAYPOINT_TO, oldValue, getWaypointTo());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// NOOP
	}

	public CapacityWaypoint getWaypointFrom() {
		return waypointFrom;
	}
	

	public CapacityWaypoint getWaypointTo() {
		return waypointTo;
	}
}