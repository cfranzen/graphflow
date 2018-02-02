package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import models.pointBased.CapacityWaypoint;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class WaypointController {

	// private List<CapacityWaypoint> waypoints = new ArrayList<>();
	private List<List<CapacityWaypoint>> zoomWaypoints = new ArrayList<>();

	/**
	 * 
	 */
	public WaypointController() {
		if (Constants.zoomAggregation) {
			for (int i = 0; i < Constants.ZOOM_LEVEL_COUNT; i++) {
				zoomWaypoints.add(new ArrayList<>());
			}
		} else {
			zoomWaypoints.add(new ArrayList<>());
		}
	}

	/**
	 * Creates {@link CapacityWaypoint}s for the given {@link List} of
	 * {@link GeoPosition}s.
	 * 
	 * @param nodes
	 *            {@link List} with {@link GeoPosition}s as nodes
	 * @return a new {@link Set} with {@link CapacityWaypoint}s
	 */
	public void createWaypointsFromGeo(List<GeoPosition> nodes) {
		List<CapacityWaypoint> waypoints = new ArrayList<>();
		if (Constants.onlyGermany) {
			for (GeoPosition geoPos : nodes) {
				double lat = geoPos.getLatitude();
				double lon = geoPos.getLongitude();
				// Only central europe
				if ((-13 < lon && lon < 15) && (40 < lat && lat < 60)) {
					waypoints.add(new CapacityWaypoint(lat, lon, 0));
				}
			}
		} else {
			for (GeoPosition geoPos : nodes) {
				CapacityWaypoint waypoint = new CapacityWaypoint(geoPos.getLatitude(), geoPos.getLongitude(), 0);
				waypoints.add(waypoint);
			}
		}
		setWaypoints(waypoints);
	}

	public void setWaypoints(List<CapacityWaypoint> savedNodes, int zoomlevel) {
		zoomlevel = zoomlevel >= zoomWaypoints.size() ? zoomWaypoints.size() - 1 : zoomlevel;
		zoomWaypoints.set(zoomlevel, savedNodes);
	}

	public List<CapacityWaypoint> getWaypoints(int zoomlevel) {
		final int ZOOM_LIMIT = Constants.MAX_ZOOM_LEVEL - 3;
		float zoomIndex = (Constants.ZOOM_LEVEL_COUNT - (Constants.ZOOM_LEVEL_COUNT
				/ (float) (Math.min(Constants.MAX_ZOOM_LEVEL, ZOOM_LIMIT) - Math.min(zoomlevel, ZOOM_LIMIT - 1))));
		zoomIndex = zoomIndex >= zoomWaypoints.size() ? zoomWaypoints.size() - 1 : zoomIndex;
		return zoomWaypoints.get((int) zoomIndex);
	}

	public void setWaypoints(List<CapacityWaypoint> waypoints) {
		zoomWaypoints.set(zoomWaypoints.size() - 1, waypoints);
	}

	public List<GeoPosition> getWaypointPositions(int zoomlevel) {
		List<GeoPosition> result = new ArrayList<>(zoomWaypoints.get(zoomlevel).size());
		for (Waypoint waypoint : zoomWaypoints.get(zoomlevel)) {
			result.add(waypoint.getPosition());
		}
		return result;
	}

	/**
	 * @return the {@link Waypoint} as {@link List}
	 */
	public List<CapacityWaypoint> getWaypoints() {
		return zoomWaypoints.get(zoomWaypoints.size() - 1);
	}

}