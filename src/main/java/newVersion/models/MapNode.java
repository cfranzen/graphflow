package newVersion.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;

import models.Edge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapNode {

	private GeoPosition posi;
	private List<Edge> edgeMap = new ArrayList<>(); 
	public GeoPosition contactPoint = null;
	public Long[][] capWork;

	/**
	 * 
	 */
	public MapNode(GeoPosition posi) {
		this.posi = posi;
	}

	public GeoPosition getPosition() {
		return posi;
	}

	/**
	 * @param edge
	 */
	public void addEdge(Edge edge) {
		if (!edgeMap.contains(edge)) {
			// So we get an unique list
			edgeMap.add(edge);
		}
	}

	public void calcCapacityAndWorkload(int timesteps) {
		capWork = new Long[timesteps][2];
		for (int currentTimeStep = 0; currentTimeStep < timesteps; currentTimeStep++) {
			long cap = 0;
			long work = 0;
			for (Edge edge : edgeMap) {
				cap += edge.getCapacity(currentTimeStep);
				work += edge.getWorkload(currentTimeStep);
			}
			capWork[currentTimeStep][0] = cap;
			capWork[currentTimeStep][1] = work;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
