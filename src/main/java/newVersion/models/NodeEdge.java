package newVersion.models;

import java.util.ArrayList;
import java.util.List;

import models.Edge;

/**
 * Contains informations about which points are on this route/edge and the
 * service times from the entities.
 * <p>
 * Capacity and workload informations are found in the {@link MapNode}-class
 * 
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class NodeEdge extends Edge {

	public List<MapNode> points = new ArrayList<>();

	/**
	 * @param currentEdge
	 */
	public NodeEdge(Edge e) {
		this.start = e.getStart();
		this.dest = e.getDest();
		this.serviceTimes = e.getServiceTimes();
	}

	/**
	 * @return
	 */
	public List<MapNode> getPoints() {
		return points;
	}

	public MapNode get(int i) {
		return points.get(i);
	}

}
