package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.util.PointList;

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
public class RouteController implements PropertyChangeListener {

	static final Logger logger = LoggerFactory.getLogger(RouteController.class);

	private List<Edge> route = new ArrayList<>();
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
		// noop
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
		this.route = route;
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
	public void importEdges(List<Edge> edges) {
		List<Edge> route =  new ArrayList<>();
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

				// Only Germany
				if ((6 < lon && lon < 14) && (45 < lat && lat < 55)) {
					route.add(edge);
				}
			}
		} else {
			// for (Edge edge : edges) {
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
	public void excludeNonVisiblePointFromPaintRoutes(GeoPosition viewportStart, GeoPosition viewportEnd) {
		excludeNonVisiblePointFromPaintRoutes(viewportStart, viewportEnd, route);
	}

	/**
	 * 
	 * @param viewportStart
	 *            {@link GeoPosition} at pixel (1,1) of the viewport
	 * @param viewportEnd
	 *            {@link GeoPosition} at last bottom-right pixel of the viewport
	 */
	public void excludeNonVisiblePointFromPaintRoutes(GeoPosition viewportStart, GeoPosition viewportEnd,
			List<Edge> route) {
		if (Constants.drawOnlyViewport) {
			// Land routes
			List<Edge> result = new ArrayList<>();
			for (Edge edge : route) {
				if ((edge.getStart() != null && pointOnScreen(edge.getStart(), viewportStart, viewportEnd))
						|| (edge.getDest() != null && pointOnScreen(edge.getDest(), viewportStart, viewportEnd))) {
					result.add(edge);
				}
			}
			paintRoute = result;

			// Sea routes
			result = new ArrayList<>();
			for (Edge edge : seaRoute) {

				if ((edge.getStart() != null && pointOnScreen(edge.getStart(), viewportStart, viewportEnd))
						|| (edge.getDest() != null && pointOnScreen(edge.getDest(), viewportStart, viewportEnd))) {
					result.add(edge);
				}
			}
			paintSeaRoute = result;
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

	public void sumAllPoints() {
		int i = 0;
		List<Edge> edges = getRoute();
		for (Edge edge : edges) {
			i += edge.getPositions().size();
		}
		logger.info(i + " Points - " + edges.size() + " Edges");

	}

	private void calcOnlyVisibleEdges(Graphics2D g, MyMap map) {
		Rectangle rect = map.getViewportBounds();
		GeoPosition viewportStart = map.getGeoPos(rect.getMinX(), rect.getMinY());
		GeoPosition viewportEnd = map.getGeoPos(rect.getMaxX(), rect.getMaxY());
		excludeNonVisiblePointFromPaintRoutes(viewportStart, viewportEnd, getRoute());
		if (Constants.debugInfos && Constants.drawOnlyViewport) {
			g.setColor(Color.MAGENTA);
			g.drawRect((int) rect.getMinX() + 300, (int) rect.getMinY() + 150,
					(int) rect.getMaxX() - 300 - ((int) rect.getMinX() + 300),
					(int) rect.getMaxY() - 150 - ((int) rect.getMinY() + 150));
		}
	}

	/**
	 * @param g
	 * @param map
	 */
	public void updatePaintRoute(Graphics2D g, MyMap map) {
		calculateSeaEdges(map);
		calcOnlyVisibleEdges(g, map);
		if (highlightedWaypointFrom != null || highlightedWaypointTo != null) {
			showOnlyWaypointEdges(getRoute(), getSeaRoute());
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
			nodeEdge.setPath(new ArrayList<>(), 0);
			nodeEdge.setPathZoom(map.getZoom());
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

}
