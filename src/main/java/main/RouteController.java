package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import com.graphhopper.util.PointList;

import gui.MyMap;
import models.CapacityWaypoint;
import models.Edge;
import models.EdgeType;
import models.HighResEdge;

/**
 * Contains the route which is to be painted from multiple sources
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RouteController {

	private static RouteController instance;
	private List<Edge> route = new ArrayList<>();
	private List<Edge> seaRoute = new ArrayList<>();
	
	private List<Edge> paintRoute = Collections.emptyList();
	
	private RouteController() { 
		//noop
	}
	
	
	/**
	 * @return the route
	 */
	public List<Edge> getRoute() {
		return route;
	}
	
	/**
	 * {@link List} with {@link HighResEdge} after mapping
	 * 
	 * @return the route
	 */
	public List<Edge> getSeaRoute() {
		return seaRoute;
	}

	public List<Edge> getRouteToPaint() {
		return paintRoute;
	}

	/**
	 * @param route the route to set
	 */
	public void setRoute(List<Edge> route) {
		this.route = route;
		updatePaintRoute();
	}

	/**
	 * @param route the route to set
	 */
	public void setSeaRoute(List<Edge> seaRoute) {
		this.seaRoute = seaRoute;
		updatePaintRoute();
	}

	/**
	 * Updates the old {@link Edge} with a new {@link Edge}. Used to update a
	 * normal {@link Edge} with a {@link HighResEdge}.
	 * 
	 * @param map 
	 * @param oldEdge
	 * @param newEdge
	 * @return <code>true</code> if oldEdge consists in the saved
	 *         {@link Edge}s</br>
	 *         <code>false</code> otherwise
	 */
	public boolean updateEdge(Edge oldEdge, Edge newEdge) {
		List<Edge> route = getRoute();
		if (newEdge != null) {
			int index = route.indexOf(oldEdge);
			if (index != -1) {
				route.set(index, newEdge);
				setRoute(route);
				return true;
			}
		} else {
			// if newEdge = Emtpy -> remove old edge instead null update
			route.remove(oldEdge);
			setRoute(route);
		}
		return false;
	}
	
	/**
	 * Updates the old {@link Edge} with a new {@link Edge}. Used to update a
	 * normal {@link Edge} with a {@link HighResEdge}.
	 * 
	 * @param map 
	 * @param oldEdge
	 * @param newEdge
	 * @return <code>true</code> if oldEdge consists in the saved
	 *         {@link Edge}s</br>
	 *         <code>false</code> otherwise
	 */
	public boolean updateSeaEdge(Edge oldEdge, Edge newEdge) {
		List<Edge> route = getSeaRoute();
		if (newEdge != null) {
			int index = route.indexOf(oldEdge);
			if (index != -1) {
				route.set(index, newEdge);
				setSeaRoute(route);
				return true;
			}
		} else {
			// if newEdge = Emtpy -> remove old edge instead null update
			route.remove(oldEdge);
			setSeaRoute(route);
		}
		return false;
	}
	
	
	/**
	 * Adds the given {@link Edge}s to the graph.
	 * 
	 * @param edges
	 *            {@link List} with {@link Edge}s.
	 */
	public void addEdges(List<Edge> edges) {
		if (MainController.onlyGermany) {
			for (Edge edge : edges) {
				double lat = 50;
				double lon = 10;
				if (edge.getStart() != null) {
					lat = edge.getStart().getLatitude();
					lon = edge.getStart().getLongitude();
				}

				// Only Germany
				if ((6 < lon && lon < 14) && (45 < lat && lat < 55)) {
					route.add(edge);
				}
			}
		} else {
			for (Edge edge : edges) {
				if (edge.getType().equals(EdgeType.VESSEL)) {
					seaRoute.add(edge);
				} else {
					route.add(edge);
				}
			}
			
//			route.addAll(edges);
		}
		setRoute(route);
	}
	
	public void importGrapHopperPoints(PointList points, MyMap map) {
		List<Edge> route = getRoute();
		Set<Waypoint> waypoints = new HashSet<>();
		GeoPosition last = new GeoPosition(points.getLatitude(0), points.getLongitude(0));
		waypoints.add(new CapacityWaypoint(last.getLatitude(), last.getLongitude(), 0));
		for (int i = 1; i < points.size(); i++) {
			GeoPosition dest = new GeoPosition(points.getLatitude(i), points.getLongitude(i));
			route.add(new Edge(last, dest));
			last = dest;
		}
		waypoints.add(new CapacityWaypoint(last.getLatitude(), last.getLongitude(), 0));
		map.setWaypoints(waypoints);
		
		setRoute(route);
	}

	/**
	 * @return
	 */
	public static RouteController getInstance() {
		if (instance == null) {
			instance = new RouteController();
		}
		return instance;
	}
	
	private void updatePaintRoute() {
		paintRoute = new ArrayList<>(route.size() + seaRoute.size());
		
		paintRoute.addAll(seaRoute);
		paintRoute.addAll(route);
	}
	

}
