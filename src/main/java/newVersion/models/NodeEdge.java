package newVersion.models;

import java.util.ArrayList;
import java.util.List;

import models.Edge;

/**
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
