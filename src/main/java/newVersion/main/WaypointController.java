package newVersion.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import models.CapacityWaypoint;
import models.Constants;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class WaypointController {

	private Set<CapacityWaypoint> waypoints = new HashSet<>();

	/**
	 * * Creates {@link CapacityWaypoint}s for the given {@link List} of
	 * {@link GeoPosition}s.
	 * 
	 * @param nodes
	 *            {@link List} with {@link GeoPosition}s as nodes
	 * @return a new {@link Set} with {@link CapacityWaypoint}s
	 */
	public void createWaypointsFromGeo(List<GeoPosition> nodes) {
		Set<CapacityWaypoint> waypoints = new HashSet<>();
		if (Constants.onlyGermany) {
			for (GeoPosition geoPos : nodes) {
				double lat = geoPos.getLatitude();
				double lon = geoPos.getLongitude();

				// Only Germany
				if ((6 < lon && lon < 14) && (45 < lat && lat < 55)) {
					waypoints.add(new CapacityWaypoint(lat, lon, 0));
				}
			}
		} else {
			for (GeoPosition geoPos : nodes) {

				CapacityWaypoint waypoint = new CapacityWaypoint(geoPos.getLatitude(), geoPos.getLongitude(), 0);

				waypoints.add(waypoint);
			}
		}
		this.waypoints = waypoints;
	}


	/**
	 * @param waypoints
	 *            the waypoints to set
	 */
	public void setWaypoints(Set<CapacityWaypoint> waypoints) {
		this.waypoints = waypoints;
	}
	
	/**
	 * @return the {@link Waypoint} positions as {@link GeoPosition}
	 */
	public List<GeoPosition> getWaypointPositions() {
		List<GeoPosition> result = new ArrayList<>(waypoints.size());
		for (Waypoint waypoint : waypoints) {
			result.add(waypoint.getPosition());
		}
		return result;
	}

	/**
	 * @return the {@link Waypoint} as {@link List}
	 */
	public List<CapacityWaypoint> getWaypointsAsList() {
		return new ArrayList<>(waypoints);
	}

	/**
	 * @return the waypoints 
	 */
	public Set<CapacityWaypoint> getWaypoints() {
		return waypoints;
	}

	
}