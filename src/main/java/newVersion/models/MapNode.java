package newVersion.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;

import models.Edge;
import models.HighResEdge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapNode {

	public GeoPosition contactPoint = null;
	public Long[][] capWork;
	private GeoPosition posi;
//	private List<Edge> edgeMap = new ArrayList<>();
	private List<Integer> edgeMap = new ArrayList<>();
	private Function<Integer, HighResEdge> searchEdge;
	
	
	/**
	 * 
	 */
	public MapNode(GeoPosition posi, Function<Integer, HighResEdge> func) {
		this.posi = posi;
		this.searchEdge = func;
	}

	public GeoPosition getPosition() {
		return posi;
	}

	/**
	 * @param edge
	 */
	public void addEdge(Edge edge) {
		if (!edgeMap.contains(edge.id)) {
			// So we get an unique list
			edgeMap.add(edge.id);
		}
	}

	public void calcCapacityAndWorkload(int timesteps) {
		capWork = new Long[timesteps][2];
		for (int currentTimeStep = 0; currentTimeStep < timesteps; currentTimeStep++) {
			long cap = 0;
			long work = 0;
			for (int edgeID : edgeMap) {
				HighResEdge edge = searchEdge.apply(edgeID);
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
