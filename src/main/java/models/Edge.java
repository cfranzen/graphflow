package models;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;

import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;
import com.syncrotess.pathfinder.model.entity.ServiceType;

/**
 * Represents an edge between to Nodes which are saved as
 * {@link GeoPosition}-objects.
 * 
 * The edge also contains informations about the capacity and workload for
 * diffrent time steps.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 */
public class Edge {

	protected GeoPosition start;
	protected GeoPosition dest;
	private int[] capacites = new int[0];
	private int[] workload = new int[0];
	private EdgeType type = EdgeType.TRUCK;
	private String info;

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
	 * @param start
	 * @param dest
	 * @param capacites
	 * @param workload
	 * @param type
	 */
	public Edge(GeoPosition start, GeoPosition dest, int[] capacites, int[] workload, EdgeType type) {
		super();
		this.start = start;
		this.dest = dest;
		this.capacites = capacites;
		this.workload = workload;
		this.type = type;
	}

	public Edge(Edge e) {
		super();
		this.start = e.start;
		this.dest = e.dest;
		this.capacites = e.capacites;
		this.workload = e.workload;
		this.type = e.type;
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
	 * Default empty constructor
	 */
	public Edge() {
		//NOP
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
		if (workload.length > currentTimeStep) {
			return workload[currentTimeStep];
		} else {
			return 0;
		}
	}

	/**
	 * @param currentTimeStep
	 * @return the capacity for the given time step.
	 */
	public int getCapacity(int currentTimeStep) {
		if (capacites.length > currentTimeStep) {
			return capacites[currentTimeStep];
		} else {
			return 0;
		}
	}

	/**
	 * @return the type of the edge
	 */
	public EdgeType getType() {
		return type;
	}

	/**
	 * Saves the corresponding {@link EdgeType} for the given
	 * {@link ServiceType}.
	 * 
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

	/**
	 * @return
	 */
	public List<GeoPosition> getPositions() {
		return Arrays.asList(
				new GeoPosition(start.getLatitude(), start.getLongitude()),
				new GeoPosition(dest.getLatitude(), dest.getLongitude()));
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
