/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import main.Controller;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapEdge extends Edge {

	public List<MapPoint> points = new ArrayList<>();
	
	// TODO refactor
	private static final List<Edge> allEdges = Controller.getInstance().getMapViewer().getRoute(); 
	
	/* (non-Javadoc)
	 * @see models.Edge#getPoints()
	 */
	@Override
	public List<Double[]> getPoints() {
		List<Double[]> returnPoints = new ArrayList<Double[]>();
		for (MapPoint mapPoint : points) {
			returnPoints.add(new Double[] {mapPoint.x, mapPoint.y});
		}
		return returnPoints;
	}
	
	/**
	 * @param currentTimeStep
	 * @param last
	 * @return
	 */
	public int getCapacityForPoint(int currentTimeStep, int index) {
		Map<Edge, Double[]> map = points.get(index).edgeMap;
		int capacity = 0;
		for (Entry<Edge,Double[]> edge : map.entrySet()) {
			capacity += edge.getKey().getCapacity(currentTimeStep);
		}
		return capacity;
	}

	/**
	 * @param currentTimeStep
	 * @param point
	 * @return
	 */
	public int getWorkloadForPoint(int currentTimeStep, int index) {
		Map<Edge, Double[]> map = points.get(index).edgeMap;
		int workload = 0;
		for (Entry<Edge,Double[]> edge : map.entrySet()) {
			workload += edge.getKey().getWorkload(currentTimeStep);
		}
		return workload;
	}
	
}
