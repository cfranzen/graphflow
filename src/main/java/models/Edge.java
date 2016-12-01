package models;

import org.jxmapviewer.viewer.GeoPosition;

import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;
import com.syncrotess.pathfinder.model.entity.ServiceType;

/**
 * Transfer object for the diffrent Edge-classes.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Edge {

	private GeoPosition start;
	private GeoPosition dest;
	private int[] capacites;
	private int[] workload;
	private EdgeType type;

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
	 * @param capacites
	 *            the capacites to set
	 */
	public void setCapacites(int[] capacites) {
		this.capacites = capacites;
	}

	/**
	 * @param workload
	 *            the workload to set
	 */
	public void setWorkload(int[] workload) {
		this.workload = workload;
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

	/**
	 * @return the type
	 */
	public EdgeType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ServiceType type) {
		switch (type) {
		case TRUCK_TRANSPORT:
			this.type = EdgeType.TRUCK;
			break;
		case TRAIN_TRANSPORT:
			this.type = EdgeType.TRAIN;
			break;
		case VESSEL_TRANSPORT:
			this.type = EdgeType.VESSEL;
			break;
		case AIRCRAFT_TRANSPORT:
			this.type = EdgeType.AIRCRAFT;
			break;
		default:
			this.type = EdgeType.UNKOWN;
		}
	}

}
