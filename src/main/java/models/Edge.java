package models;

import org.jxmapviewer.viewer.GeoPosition;

import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;

/**
 * Transfer object for the diffrent Edge-classes.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Edge {

	private GeoPosition start;
	private GeoPosition dest;
	public int[] capacites;
	public int[] workload;

	/**
	 * Creates an {@link Edge} with the given {@link GeoPosition}s.
	 * 
	 * @param start
	 * @param dest
	 */
	public Edge(GeoPosition start, GeoPosition dest) {
		super();
		this.start = start;
		this.dest = dest;
	}

	/**
	 * Creates an {@link Edge} from a given {@link Service} object.
	 * 
	 * @param edge
	 */
	public Edge(Service edge) {
		Node start = edge.getStartNode();
		Node end = edge.getEndNode();
		new Edge(new GeoPosition(start.getLatitude(), start.getLongitude()),
				new GeoPosition(end.getLatitude(), end.getLongitude()));

	}

	/**
	 * @return the start
	 */
	public GeoPosition getStart() {
		return start;
	}

	/**
	 * @return the dest
	 */
	public GeoPosition getDest() {
		return dest;
	}

	/**
	 * @param currentTimeStep
	 * @return the workload for the given time step.
	 */
	public int getWorkload(int currentTimeStep) {
		return workload[currentTimeStep];
	}

	/**
	 * @param currentTimeStep
	 * @return the capacity for the given time step.
	 */
	public int getCapacity(int currentTimeStep) {
		return capacites[currentTimeStep];
	}

}
