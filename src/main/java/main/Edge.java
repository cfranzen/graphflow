/**
 * 
 */
package main;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Edge {

	public GeoPosition start;
	public GeoPosition end;
	public double maxCapacity;
	public double workload;
	/**
	 * @param start
	 * @param end
	 * @param maxCapacity
	 * @param workload
	 */
	public Edge(GeoPosition start, GeoPosition end, double maxCapacity, double workload) {
		super();
		this.start = start;
		this.end = end;
		this.maxCapacity = maxCapacity;
		this.workload = workload;
	}


}
