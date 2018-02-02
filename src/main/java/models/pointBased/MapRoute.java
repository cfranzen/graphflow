package models.pointBased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jxmapviewer.viewer.GeoPosition;

import main.MainController;
import main.Optimizer;

/**
 * Represents a visible Edge
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapRoute extends Edge {

	private List<MapPoint> points = new ArrayList<>();

	private List<Double> serviceTimes = new ArrayList<>();

	public MapRoute() {
		// NOOP
	};

	/**
	 * @param mapedge
	 */
	public MapRoute(MapRoute mapedge) {
		super(mapedge);
	}

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
		if (index >= points.size()) {
			index = 0;
		}

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
		if (index >= points.size()) {
			index = 0;
		}

		Map<Edge, GeoPosition> map = points.get(index).edgeMap;

		int workload = 0;
		for (Entry<Edge, GeoPosition> edge : map.entrySet()) {
			workload += edge.getKey().getWorkload(currentTimeStep);
		}
		return workload;
	}

	/**
	 * Returns the service time for the specified time step. The service times
	 * are lazy calculated. The {@link List} is cleared after every point
	 * adding.
	 * 
	 * @return the calculated service time for the whole {@link MapRoute} and
	 *         given time.
	 */
	public double getServiceTime(int currentTimeStep) {
		if (serviceTimes.size() <= currentTimeStep) {
			serviceTimes.addAll(Collections.nCopies((currentTimeStep + 1) - serviceTimes.size(), null));
		}

		if (serviceTimes.get(currentTimeStep) == null) {
			double serviceTime = 0;
			for (int index = 0; index < points.size(); index++) {
				double time = getServiceTimeForPoint(currentTimeStep, index);
				if (time != 0) {
					serviceTime += time;
				}

			}
			serviceTimes.set(currentTimeStep, serviceTime);
		}

		return serviceTimes.get(currentTimeStep);
	}

	/**
	 * Calculates the service time for one specific point and time step.
	 * 
	 * @param currentTimeStep
	 * @param index
	 *            of the point
	 * @return the calculated service time per point
	 */
	private double getServiceTimeForPoint(int currentTimeStep, int index) {
		Map<Edge, GeoPosition> map = points.get(index).edgeMap;

		double serviceTime = 0;
		int edgeCount = 0;
		for (Entry<Edge, GeoPosition> edgeEntry : map.entrySet()) {
			Edge edge = edgeEntry.getKey();

			double time = edge.getServiceTime(currentTimeStep);
			if (time != 0) {
				serviceTime += time / edge.getPositions().size();
				edgeCount++;
			}
		}
		return serviceTime / edgeCount;
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

	/**
	 * Adds a new {@link MapPoint} to the {@link MapRoute}. It searches for any
	 * near {@link Edge} at the points position, which is needed for workload
	 * and capacity calculation.</br>
	 * This method clears the serviceTimes-{@link List}.
	 * 
	 * @param point
	 * @param edges
	 */
	public void addNewPoint(GeoPosition point, List<Edge> edges) {
		MapPoint mp = new MapPoint(point,
				Optimizer.getNearEdges(point, edges, MainController.combinePointsDistance));
		addPoint(mp);
		serviceTimes.clear();
	}

	/**
	 * Point to be drawn, contains informations about all underlying
	 * {@link Edge}s. Normally created while combining multiple {@link Edge}s to
	 * one.
	 * 
	 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
	 *
	 */
	public class MapPoint {

		private GeoPosition posi;
		public Map<Edge, GeoPosition> edgeMap;
		public GeoPosition contactPoint = null;

		/**
		 * @param x
		 * @param y
		 * @param edges
		 */
		public MapPoint(double x, double y, Map<Edge, GeoPosition> edgeMap) {
			super();
			this.posi = new GeoPosition(x, y);
			this.edgeMap = edgeMap;
		}

		/**
		 * @param point
		 * @param nearEdges
		 */
		public MapPoint(GeoPosition point, Map<Edge, GeoPosition> edgeMap) {
			super();
			this.posi = point;
			this.edgeMap = edgeMap;

		}

		public GeoPosition getPosition() {
			return posi;
		}

	}

	/**
	 * 
	 */
	public void updateStartEnd() {
		setStart(points.get(0).posi);
		setDest(points.get(points.size() - 1).posi);
	}

	/**
	 * @param savedPoint
	 */
	public void removePoint(GeoPosition savedPoint) {
		points.remove(savedPoint);
	}

}
