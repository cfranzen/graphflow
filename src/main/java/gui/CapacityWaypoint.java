/**
 * 
 */
package gui;

import org.jxmapviewer.beans.AbstractBean;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class CapacityWaypoint extends AbstractBean implements Waypoint {

	private GeoPosition position;
	private double maxCapacity;
	private double workload = 0;

	/**
	 * Creates a new instance of Waypoint
	 */
	public CapacityWaypoint(double maxCapacity) {
		this.position = new GeoPosition(0, 0);
		this.maxCapacity = maxCapacity;
	}

	/**
	 * @param latitude
	 *            the latitude
	 * @param longitude
	 *            the longitude
	 */
	public CapacityWaypoint(double latitude, double longitude, double maxCapacity) {
		this.position = new GeoPosition(latitude, longitude);
		this.maxCapacity = maxCapacity;
	}

	/**
	 * @param coord
	 *            the geo coordinate
	 */
	public CapacityWaypoint(GeoPosition coord, double maxCapacity) {
		this.position = coord;
		this.maxCapacity = maxCapacity;
	}

	/**
	 * Set a new GeoPosition for this Waypoint
	 * 
	 * @param coordinate
	 *            a new position
	 */
	public void setPosition(GeoPosition coordinate) {
		GeoPosition old = getPosition();
		this.position = coordinate;
		firePropertyChange("position", old, getPosition());
	}

	/**
	 * @return the maxCapacity
	 */
	public double getMaxCapacity() {
		return maxCapacity;
	}

	/**
	 * @param maxCapacity the maxCapacity to set
	 */
	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	/**
	 * @return the workload
	 */
	public double getWorkload() {
		return workload;
	}

	/**
	 * @param workload the workload to set
	 */
	public void setWorkload(double workload) {
		this.workload = workload;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jxmapviewer.viewer.Waypoint#getPosition()
	 */
	@Override
	public GeoPosition getPosition() {
		return position;
	}

}
