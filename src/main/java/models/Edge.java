package models;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.util.DistanceCalc3D;
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

	private static final DistanceCalc3D calulator = new DistanceCalc3D();

	public int id;
	protected GeoPosition start;
	protected GeoPosition dest;
	private long[] capacites = new long[0];
	private long[] workloads = new long[0];
	private EdgeType type = EdgeType.TRUCK;
	private String info;
	private double distance;
	

	
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
		calcDistance();
	}
	
	/**
	 * @param start
	 * @param dest
	 * @param capacites
	 * @param workload
	 * @param type
	 */
	public Edge(GeoPosition start, GeoPosition dest, long[] capacites, long[] workload, EdgeType type) {
		super();
		this.start = start;
		this.dest = dest;
		this.capacites = capacites;
		this.workloads = workload;
		this.type = type;
		calcDistance();
	}

	public Edge(Edge e) {
		super();
		this.id = e.id;
		this.start = e.start;
		this.dest = e.dest;
		this.capacites = e.capacites;
		this.workloads = e.workloads;
		this.type = e.type;
		this.info = e.info;
		calcDistance();
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
		calcDistance();
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
	public void setCapacites(long[] capacites) {
		this.capacites = capacites;
	}

	/**
	 * @param workload
	 *            the workload to set
	 */
	public void setWorkload(long[] workload) {
		this.workloads = workload;
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
	public long getWorkload(int currentTimeStep) {
		if (workloads.length > currentTimeStep) {
			return workloads[currentTimeStep];
		} else {
			return 0;
		}
	}

	public long[] getWorkloads() {
		return workloads;
	}
	
	public void addWorkloads(long[] ls) {
		for (int i = 0; i < ls.length; i++) {
			this.workloads[i] += ls[i];
		}
	}

	
	/**
	 * @param currentTimeStep
	 * @return the capacity for the given time step.
	 */
	public long getCapacity(int currentTimeStep) {
		if (capacites.length > currentTimeStep) {
			return capacites[currentTimeStep];
		} else {
			return 0;
		}
	}
	
	public long[] getCapacites() {
		return capacites;
	}

	/**
	 * @param ls
	 */
	public void addCapacities(long[] ls) {
		for (int i = 0; i < ls.length; i++) {
			this.capacites[i] += ls[i];
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

	/**
	 * @return
	 */
	public double getDistance() {
		return distance;
	}
	
	private void calcDistance() {
		distance = calulator.calcDist(start.getLatitude(), start.getLongitude(), dest.getLatitude(), dest.getLongitude());
	}

}
