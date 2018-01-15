package main;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jxmapviewer.beans.AbstractBean;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.util.PointList;

import dto.RouteControllerDTO;
import gui.MyMap;
import models.CapacityWaypoint;
import models.Constants;
import models.Edge;
import models.EdgeType;
import models.HighResEdge;
import models.SeaEdge;
import newVersion.models.NodeEdge;
import painter.SeaRoutePainter;

/**
 * Contains the route which is to be painted from multiple sources
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RouteController extends AbstractBean implements PropertyChangeListener {

	static final Logger logger = LoggerFactory.getLogger(RouteController.class);

	// private List<Edge> route = new ArrayList<>();
	private List<List<Edge>> routes = new ArrayList<>();

	private HashMap<Integer, HighResEdge> highResEdgeMap;

	private List<Edge> seaRoute = new ArrayList<>();
	private CapacityWaypoint highlightedWaypointFrom = null;
	private CapacityWaypoint highlightedWaypointTo = null;

	/**
	 * Contains the visible route part
	 */
	private List<Edge> paintRoute = new ArrayList<>();

	/**
	 * Contains the visible sea route part
	 */
	private List<Edge> paintSeaRoute = new ArrayList<>();

	RouteController() {
		if (Constants.zoomAggregation) {
			for (int i = 0; i < Constants.ZOOM_LEVEL_COUNT; i++) {
				routes.add(new ArrayList<>());
			}
		} else {
			routes.add(new ArrayList<>());
		}
		highResEdgeMap = new HashMap<>();
	}

	/**
	 * @param routeControllerDTO
	 */
	public RouteController(RouteControllerDTO routeControllerDTO) {
		this.routes = routeControllerDTO.routes.stream()
				.map(e -> e.stream().map(n -> (Edge) n).collect(Collectors.toList())).collect(Collectors.toList());

		this.highResEdgeMap = routeControllerDTO.highResEdgeMap;
		this.seaRoute = routeControllerDTO.seaRoute.stream().map(e -> (Edge) e).collect(Collectors.toList());
	}

	public List<List<Edge>> getAllRoutes() {
		return routes;
	}

	/**
	 * @return the route
	 */
	public List<Edge> getRoute() {
		return routes.get(routes.size() - 1);
	}

	public List<Edge> getRoute(int index) {
		index = index >= routes.size() ? routes.size() - 1 : index;
		logger.debug("Got route: " + index);
		return routes.get(index);
	}

	public List<Edge> getRouteByZoom(int zoomlevel) {
		final int ZOOM_LIMIT = Constants.MAX_ZOOM_LEVEL - 3;
		float zoomIndex = (Constants.ZOOM_LEVEL_COUNT - (Constants.ZOOM_LEVEL_COUNT
				/ (float) (Math.min(Constants.MAX_ZOOM_LEVEL, ZOOM_LIMIT) - Math.min(zoomlevel, ZOOM_LIMIT - 1))));
		if (Constants.debugInfos) {
			logger.info("ZoomLevelBasis: " + zoomIndex);
		}

		return getRoute((int) zoomIndex);

		// return getRoute(Math.min(Constants.ZOOM_LEVEL_COUNT - 1, Math.max(0,
		// (int) (Constants.ZOOM_LEVEL_COUNT * 2
		// / (float) (Constants.MAX_ZOOM_LEVEL) * (Constants.MAX_ZOOM_LEVEL -
		// zoomlevel)))));
	}

	/**
	 * {@link List} with {@link HighResEdge} after mapping
	 * 
	 * @return the route
	 */
	public List<Edge> getSeaRoute() {
		return seaRoute;
	}

	public List<Edge> getPaintRoute() {
		return paintRoute;
	}

	public List<Edge> getPaintSeaRoute() {
		return paintSeaRoute;
	}

	/**
	 * @param route
	 *            the route to set
	 */
	public void setRoute(List<Edge> route) {
		routes.set(routes.size() - 1, route);
	}

	public void setRoute(List<Edge> route, int index) {
		index = index >= routes.size() ? routes.size() - 1 : index;
		routes.set(index, route);
	}

	/**
	 * @param route
	 *            the route to set
	 */
	public void setSeaRoute(List<Edge> seaRoute) {
		this.seaRoute = seaRoute;
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
		return updateEdge(oldEdge, newEdge, Constants.ZOOM_LEVEL_COUNT - 1);
	}

	public boolean updateEdge(Edge oldEdge, Edge newEdge, int i) {
		List<Edge> route = getRoute(i);
		if (newEdge != null) {
			int index = route.indexOf(oldEdge);
			if (index != -1) {
				route.set(index, newEdge);
				setRoute(route, i);
				firePropertyChange(Constants.EVENT_NAME_EDGE_CHANGE, oldEdge, newEdge);
				return true;
			}
		} else {
			// if newEdge = Emtpy -> remove old edge instead null update
			route.remove(oldEdge);
			setRoute(route, i);
		}
		return false;
	}

	/**
	 * 
	 */

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
	public void importEdges(List<Edge> edges) {
		List<Edge> route = new ArrayList<>();
		if (Constants.onlyGermany) {
			// for (Edge edge : edges) {
			for (int i = 0; i < edges.size(); i++) {
				Edge edge = edges.get(i);
				edge.id = i;
				double lat = 50;
				double lon = 10;
				if (edge.getStart() != null) {
					lat = edge.getStart().getLatitude();
					lon = edge.getStart().getLongitude();
				}

				// Only central europe
				if ((-13 < lon && lon < 15) && (40 < lat && lat < 60)) {
					route.add(edge);
				}
			}
		} else {
			for (int i = 0; i < edges.size(); i++) {
				Edge edge = edges.get(i);
				edge.id = i;
				if (edge.getType().equals(EdgeType.VESSEL)) {
					seaRoute.add(edge);
				} else {
					route.add(edge);
				}
			}

		}
		setRoute(route);
	}

	public void importGrapHopperPoints(PointList points, MyMap map) {
		List<Edge> route = getRoute();
		Set<CapacityWaypoint> waypoints = new HashSet<>();
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
	 * @param viewportStart
	 * @param viewportEnd
	 */
	public void excludeNonVisiblePointFromPaintRoutes(MyMap map) {
		excludeNonVisiblePointFromPaintRoutes(map, getRoute());
	}

	/**
	 * 
	 * @param viewportStart
	 *            {@link GeoPosition} at pixel (1,1) of the viewport
	 * @param viewportEnd
	 *            {@link GeoPosition} at last bottom-right pixel of the viewport
	 */
	public void excludeNonVisiblePointFromPaintRoutes(MyMap map, List<Edge> route) {
		if (Constants.drawOnlyViewport) {

			Rectangle rect = map.getViewportBounds();
			GeoPosition viewportStart = map.getGeoPos(rect.getMinX(), rect.getMinY());
			GeoPosition viewportEnd = map.getGeoPos(rect.getMaxX(), rect.getMaxY());

			// Land routes
			List<Edge> result = new ArrayList<>();
			for (Edge edge : route) {
				if ((edge.getStart() != null && pointOnScreen(edge.getStart(), viewportStart, viewportEnd))
						|| (edge.getDest() != null && pointOnScreen(edge.getDest(), viewportStart, viewportEnd))) {
					result.add(edge);
				}
			}
			paintRoute = result;

			// Sea routes - show always all edges,
			// result = new ArrayList<>();
			// for (Edge edge : seaRoute) {
			//
			// if ((edge.getStart() != null && pointOnScreen(edge.getStart(),
			// viewportStart, viewportEnd))
			// || (edge.getDest() != null && pointOnScreen(edge.getDest(),
			// viewportStart, viewportEnd))) {
			// result.add(edge);
			// }
			// }
			// paintSeaRoute = result;
			paintSeaRoute = seaRoute;
		} else {
			paintRoute = route;
			paintSeaRoute = seaRoute;
		}
	}

	/**
	 * @param pos
	 * @param viewportStart
	 * @param viewportEnd
	 * @return
	 */
	private boolean pointOnScreen(GeoPosition pos, GeoPosition viewportStart, GeoPosition viewportEnd) {

		return ((viewportStart.getLatitude() > pos.getLatitude() && pos.getLatitude() > viewportEnd.getLatitude())
				&& (viewportStart.getLongitude() < pos.getLongitude()
						&& pos.getLongitude() < viewportEnd.getLongitude()));
	}

	public void sumRoutePoints() {
		int i = 0;
		List<Edge> edges = getRoute();
		for (Edge edge : edges) {
			i += edge.getPositions().size();
		}
		logger.info(i + " Points - " + edges.size() + " Edges");

	}

	private void calcOnlyVisibleEdges(Graphics2D g, MyMap map, List<Edge> route) {
		excludeNonVisiblePointFromPaintRoutes(map, route);
	}

	/**
	 * @param g
	 * @param map
	 */
	public void updatePaintRoute(Graphics2D g, MyMap map) {
		calculateSeaEdges(map);
		List<Edge> route = getRouteByZoom(map.getZoom());

		// route = routes.get(1);

		calcOnlyVisibleEdges(g, map, route);
		route = paintRoute;
		if (highlightedWaypointFrom != null || highlightedWaypointTo != null) {
			showOnlyWaypointEdges(route, getSeaRoute());
		}
	}

	private void calculateSeaEdges(MyMap map) {
		List<NodeEdge> seaRoute = new ArrayList<>();
		for (Edge edge : getSeaRoute()) {
			if (!(edge instanceof NodeEdge)) {
				return;
			}
			seaRoute.add((NodeEdge) edge);
		}
		List<SeaEdge> seaEdges = SeaRoutePainter.calcDrawEdges(seaRoute);
		for (NodeEdge nodeEdge : seaRoute) {
			nodeEdge.setPath(new ArrayList<>(), map.getZoom());
			for (SeaEdge seaEdge : seaEdges) {
				if (seaEdge.edgeIds.contains(nodeEdge.id)) {
					nodeEdge.addToPath(seaEdge.getPath());
				}
			}
		}
	}

	/**
	 * Filters the given {@link List}s to that only {@link Edge}s which have the
	 * saved {@link Waypoint} as start positon are drawn.
	 */
	private void showOnlyWaypointEdges(List<Edge> route, List<Edge> seaRoute) {

		List<Edge> result = new ArrayList<>();
		GeoPosition from = highlightedWaypointFrom == null ? new GeoPosition(0, 0)
				: highlightedWaypointFrom.getPosition();
		GeoPosition to = highlightedWaypointTo == null ? new GeoPosition(0, 0) : highlightedWaypointTo.getPosition();

		for (Edge edge : route) {
			if (edge.getStart().equals(from) || edge.getDest().equals(to)) {
				result.add(edge);
			}
		}
		paintRoute = result;

		result = new ArrayList<>();
		for (Edge edge : seaRoute) {
			if (edge.getStart().equals(from) || edge.getDest().equals(to)) {
				result.add(edge);
			}
		}
		paintSeaRoute = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if (Constants.EVENT_NAME_WAYPOINT_FROM.equals(propertyName)) {
			highlightedWaypointFrom = (CapacityWaypoint) evt.getNewValue();
		}
		if (Constants.EVENT_NAME_WAYPOINT_TO.equals(propertyName)) {
			highlightedWaypointTo = (CapacityWaypoint) evt.getNewValue();
		}

	}

	/**
	 * @param list
	 */
	public void saveHighResEdge(List<Edge> list) {
		for (Edge e : list) {
			if (e instanceof HighResEdge) {
				highResEdgeMap.put(e.id, (HighResEdge) e);
			}
		}
	}

	public HighResEdge searchEdgeById(int id) {
		return highResEdgeMap.get(id);
	}

	/**
	 * @return
	 */
	public HashMap<Integer, HighResEdge> getHighResEdgeMap() {
		return highResEdgeMap;
	}

}
