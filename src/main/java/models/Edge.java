package models;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.util.DistanceCalc3D;
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
	protected int[] serviceTimes = new int[0];
	protected long[] capacites = new long[0];
	protected long[] workloads = new long[0];
	private int maxServiceTime = 0;

	private EdgeType type = EdgeType.TRUCK;
	private String info;
	private double geoDistance;

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
		this.geoDistance = calcDistance(start, dest);
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
		this.geoDistance = calcDistance(start, dest);
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
		this.serviceTimes = e.serviceTimes;
		this.maxServiceTime = e.maxServiceTime;
		this.geoDistance = calcDistance(start, dest);
	}

	/**
	 * Creates an {@link Edge} from a given {@link Service} object.
	 * 
	 * @param edge
	 */
	public Edge(Service edge) {
		GeoPosition start = new GeoPosition(edge.getStartNode().getLatitude(), edge.getStartNode().getLongitude());
		GeoPosition dest = new GeoPosition(edge.getEndNode().getLatitude(), edge.getEndNode().getLongitude());
		new Edge(start, dest);
		this.geoDistance = calcDistance(start, dest);
	}

	/**
	 * Default empty constructor
	 */
	public Edge() {
		// NOP
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
			if (currentTimeStep < 0) {
				return workloads[0];
			}
			return workloads[currentTimeStep];
		} else {
			return workloads[0];
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
			if (currentTimeStep < 0) {
				return capacites[0];
			}
			return capacites[currentTimeStep];
		} else {
			return capacites[0];
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
		return Arrays.asList(new GeoPosition(start.getLatitude(), start.getLongitude()),
				new GeoPosition(dest.getLatitude(), dest.getLongitude()));
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return
	 */
	public double getGeoDistance() {
		return geoDistance;
	}

	public static double calcDistance(GeoPosition start, GeoPosition dest) {
		if (start != null && dest != null) {
			return calulator.calcDist(start.getLatitude(), start.getLongitude(), dest.getLatitude(),
					dest.getLongitude());
		}
		return 0;
	}

	/**
	 * @param serviceTimes
	 */
	public int[] getServiceTimes() {
		return serviceTimes;
	}

	/**
	 * @param serviceTimes
	 */
	public double getServiceTime(int timeStep) {
		if (timeStep < serviceTimes.length) {
			return serviceTimes[timeStep];
		} else {
			return 1;
		}
	}

	/**
	 * @param serviceTime
	 */
	public void setServiceTime(int[] serviceTime) {
		this.serviceTimes = serviceTime;
	}

	/**
	 * @return the maxServiceTime
	 */
	public int getMaxServiceTime() {
		return maxServiceTime;
	}

	/**
	 * @param maxServiceTime
	 *            the maxServiceTime to set
	 */
	public void setMaxServiceTime(int maxServiceTime) {
		this.maxServiceTime = maxServiceTime;
	}

	/**
	 * @param contactPoint
	 */
	public void setStart(GeoPosition contactPoint) {
		this.start = contactPoint;
	}

	/**
	 * @param contactPoint
	 */
	public void setDest(GeoPosition contactPoint) {
		this.dest = contactPoint;
	}

}
