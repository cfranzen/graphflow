/**
 * 
 */
package models;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.jxmapviewer.beans.AbstractBean;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import main.MainController;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class CapacityWaypoint extends AbstractBean implements Waypoint {

	private GeoPosition position;
	private double maxCapacity;
	private double workload = 0;
	private Point mapPosi;

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
	 * @param geo
	 *            the geo coordinate
	 */
	public CapacityWaypoint(GeoPosition geo, double maxCapacity) {
		this.position = geo;
		this.maxCapacity = maxCapacity;
	}

	/**
	 * Set a new GeoPosition for this Waypoint
	 * 
	 * @param geo
	 *            a new position
	 */
	public void setPosition(GeoPosition geo) {
		GeoPosition old = getPosition();
		this.position = geo;
		setMapPosiFromGeo(geo);
		firePropertyChange("position", old, getPosition());
	}

	/**
	 * @return the maxCapacity
	 */
	public double getMaxCapacity() {
		return maxCapacity;
	}

	/**
	 * @param maxCapacity
	 *            the maxCapacity to set
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
	 * @param workload
	 *            the workload to set
	 */
	public void setWorkload(double workload) {
		this.workload = workload;
	}

	public void setMapPosiFromGeo(GeoPosition geo) {
		Point2D p = MainController.getInstance().getMapViewer().convertGeoPositionToPoint(geo);
		setMapPosi(p.getX(), p.getY());
	}

	public void setMapPosi(double x, double y) {
		mapPosi.setLocation(x, y);
	}

	/**
	 * @return the mapPosi
	 */
	public Point getMapPosi() {
		return mapPosi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Latitude: %f\nLongitude: %f\nCapacity: %f\nWorkload: %f",
				position.getLatitude(), position.getLongitude(), maxCapacity, workload);
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
