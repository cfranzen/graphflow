package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapEdge extends Edge {

	private List<MapPoint> points = new ArrayList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see models.Edge#getPoints()
	 */
	@Override
	public List<GeoPosition> getPositions() {
		List<GeoPosition> returnPoints = new ArrayList<>();
		for (MapPoint mapPoint : points) {
			returnPoints.add(mapPoint.getPosition());
		}
		return returnPoints;
	}

	/**
	 * @param currentTimeStep
	 * @param last
	 * @return
	 */
	public int getCapacityForPoint(int currentTimeStep, int index) {
		Map<Edge, GeoPosition> map = points.get(index).edgeMap;
		int capacity = 0;
		for (Entry<Edge, GeoPosition> edge : map.entrySet()) {
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
		Map<Edge, GeoPosition> map = points.get(index).edgeMap;
		
		int workload = 0;
		for (Entry<Edge, GeoPosition> edge : map.entrySet()) {
			workload += edge.getKey().getWorkload(currentTimeStep);
		}
		return workload;
	}

	/**
	 * @param j
	 * @return
	 */
	public GeoPosition getContactPointForIndex(int index) {
		List<MapPoint> pointList = new ArrayList<>(points);
		Collections.copy(pointList, points);
		pointList.add(new MapPoint(dest, Collections.emptyMap()));
		return pointList.get(index).contactPoint;
	}

	/**
	 * @param mp
	 */
	public void addPoint(MapPoint mp) {
		points.add(mp);
	}

	/**
	 * @return
	 */
	public List<MapPoint> getPoints() {
		return points;
	}

}
