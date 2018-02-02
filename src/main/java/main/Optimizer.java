/**
 * 
 */
package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import models.nodeBased.MapNode;
import models.nodeBased.NodeEdge;
import models.pointBased.CapacityWaypoint;
import models.pointBased.Edge;
import models.pointBased.EdgeType;
import models.pointBased.HighResEdge;
import models.pointBased.MapRoute;
import models.pointBased.MapRoute.MapPoint;
import painter.EntityFlowPainter;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Optimizer {

	/**
	 * used for removing multiple point which are near together, distance in
	 * lat/lon not pixel
	 */
	private static final double reduceEdgeDistance = 0.01075;

	private static final Logger logger = LoggerFactory.getLogger(Optimizer.class);
	private ExecutorService service;
	// private CompletionService<Boolean> service;

	/**
	 * @param routeController
	 * 
	 */
	public Optimizer() {
		service = Executors.newCachedThreadPool();
		// service = Executors.newFixedThreadPool(3);
		// service = new
		// ExecutorCompletionService<>(Executors.newCachedThreadPool());

	}

	/**
	 * Changes the edge structure from simple lists with saved
	 * {@link GeoPosition}s for the path, to {@link MapNode} based edges. The
	 * advantage from this is, that edges, which have identical parts, only one
	 * node is saved. This is also the basis for node coloring for the
	 * {@link EntityFlowPainter}.
	 * 
	 * @param routeController
	 * @param waypointController
	 */
	public void optimize(RouteController routeController, WaypointController waypointController) {
		routeController.sumRoutePoints();
		logger.info("optimize v2");
		long time = System.currentTimeMillis();

		// combine edges & create almost empty map points
		// Creates MapNodes for each simple point in the list
		if (Constants.optimzeLandRoutes) {
			for (int i = 0; i < routeController.getAllRoutes().size(); i++) {
				routeController
						.setRoute(optimizeGivenEdges(routeController.getRoute(i), routeController::searchEdgeById), i);
			}
			logger.info("land edges done - now processing sea edges");
		}

		List<Edge> seaRoute = optimizeGivenEdges(routeController.getSeaRoute(), routeController::searchEdgeById);
		routeController.setSeaRoute(seaRoute);

		routeController.sumRoutePoints();
		logger.info("optimize v2-end | " + (System.currentTimeMillis() - time) + " ms");
	}

	/**
	 * @param routeController
	 * @param waypointController
	 */
	public static void aggregateWaypoints(RouteController routeController, WaypointController waypointController) {
		long time = System.currentTimeMillis();
		logger.info("Aggregate waypoints");

		for (int level = 1; level < Constants.ZOOM_LEVEL_COUNT; level++) {
			logger.info("Run - " + level);
			List<Edge> route = new ArrayList<>(routeController.getRoute());
			routeController.setRoute(route, level - 1);

			List<CapacityWaypoint> savedNodes = new ArrayList<>();
			for (CapacityWaypoint waypoint : waypointController.getWaypoints()) {
				CapacityWaypoint node = getNearWaypoint(waypoint.getPosition(), savedNodes,
						Constants.ZOOM_LEVEL_DISTANCE_GEO / level);
				if (node == null) {
					savedNodes.add(waypoint);
				} else {
					for (int i = 0; i < route.size(); i++) {
						Edge edge = route.get(i);
						Edge newEdge = new Edge(edge);

						if (edge.getStart().equals(waypoint.getPosition())) {
							newEdge.setStart(node.getPosition());
						}
						if (edge.getDest().equals(waypoint.getPosition())) {
							newEdge.setDest(node.getPosition());
						}
						if (!newEdge.getStart().equals(newEdge.getDest())) {
							route.set(i, newEdge);
						}
						routeController.updateEdge(edge, newEdge, level - 1);
					}

					for (Edge edge : routeController.getSeaRoute()) {
						Edge newEdge = new Edge(edge);
						if (edge.getStart().equals(waypoint.getPosition())) {
							newEdge.setStart(node.getPosition());
						}
						if (edge.getDest().equals(waypoint.getPosition())) {
							newEdge.setDest(node.getPosition());
						}
						routeController.updateSeaEdge(edge, newEdge);
					}
				}

			}
			waypointController.setWaypoints(savedNodes, level - 1);
		}

		logger.info("Aggregate waypoints end - " + (System.currentTimeMillis() - time) + " ms");
	}

	private List<Edge> optimizeGivenEdges(List<Edge> edges, Function<Integer, HighResEdge> searchFunc) {
		if (edges == null || edges.isEmpty()) {
			return Collections.emptyList();
		}
		List<Edge> savedEdges = new ArrayList<>();
		List<MapNode> savedNodes = new ArrayList<>();
		for (int i = 0; i < edges.size(); i++) {
			long time = System.currentTimeMillis();
			Edge currentEdge = edges.get(i);
			NodeEdge mapEdge = new NodeEdge(currentEdge);
			for (GeoPosition point : currentEdge.getPositions()) {
				MapNode node = getNearestNode(point, savedNodes);
				if (node == null) {
					node = new MapNode(point);
					savedNodes.add(node);
				}
				node.addEdge(currentEdge);
				mapEdge.nodes.add(node);
				mapEdge.id = i;
			}
			if (mapEdge.nodes.size() > 5) {
				savedEdges.add(mapEdge);
			}
			logger.info("Edge " + i + " from " + edges.size() + " processed : " + mapEdge.nodes.size() + " Nodes - "
					+ (System.currentTimeMillis() - time) + " ms | Added: " + (mapEdge.nodes.size() > 5));
		}
		calcCapacityWorkload(savedNodes, Constants.timesteps, searchFunc);
		return savedEdges;
	}

	private void calcCapacityWorkload(List<MapNode> savedNodes, int timesteps,
			Function<Integer, HighResEdge> searchFunc) {
		logger.info("Precalculate Capacity and Workload");
		// for each map point, calculate work&cap

		List<Callable<Boolean>> callables = new ArrayList<>();
		savedNodes.forEach((mapNode) -> callables.add(() -> {
			mapNode.calcCapacityAndWorkload(timesteps, searchFunc);
			return true;
		}));
		createFuturesForCallables(callables);
	}

	private void createFuturesForCallables(List<Callable<Boolean>> runnables) {
		createFuturesForCallables(runnables, 300000);
	}

	private void createFuturesForCallables(List<Callable<Boolean>> runnables, int maxTimeout) {
		List<Future<Boolean>> list = null;
		try {
			list = service.invokeAll(runnables, maxTimeout, TimeUnit.MILLISECONDS);
			for (Future<Boolean> future : list) {
				future.get(maxTimeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e1) {
			e1.printStackTrace();
		}

	}

	public void mapEdgesToStreets(GraphHopper graphHopper, RouteController routeController,
			SeaNodeFactory seaController) {
		if (Constants.optimzeLandRoutes) {
			for (int i = 0; i < Constants.ZOOM_LEVEL_COUNT; i++) {
				generateStreetTasks(graphHopper, routeController, seaController, routeController.getRoute(i), i);
			}
		}
		routeController.saveHighResEdge(routeController.getRoute());
		generateStreetTasks(graphHopper, routeController, seaController, routeController.getSeaRoute(), 0);
		routeController.saveHighResEdge(routeController.getSeaRoute());

	}

	private void generateStreetTasks(GraphHopper graphHopper, RouteController routeController,
			SeaNodeFactory seaController, List<Edge> route, int i) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		route.forEach((edge) -> callables.add(() -> {
			HighResEdge highResEdge;
			if (edge.getType().equals(EdgeType.VESSEL)) {

				DijkstraAlgorithm dijkstraAlgorithm;
				dijkstraAlgorithm = new DijkstraAlgorithm(seaController.getEdges());
				dijkstraAlgorithm.execute(edge.getStart());
				List<GeoPosition> steps = dijkstraAlgorithm.getPath(edge.getDest());
				highResEdge = new HighResEdge(edge);
				highResEdge.addPositions(steps);
				logger.info("map edge to sea route DONE - " + edge.id);
				return routeController.updateSeaEdge(edge, highResEdge);
			} else {
				highResEdge = getHighRes(graphHopper, edge);
				logger.info("map edge to street DONE - " + edge.id);
				return routeController.updateEdge(edge, highResEdge, i);
			}
		}));
		createFuturesForCallables(callables);

	}

	private HighResEdge getHighRes(GraphHopper graphHopper, Edge edge) {
		GHPoint start = new GHPoint(edge.getStart().getLatitude(), edge.getStart().getLongitude());
		GHPoint dest = new GHPoint(edge.getDest().getLatitude(), edge.getDest().getLongitude());

		GHRequest ghRequest = new GHRequest(start, dest);
		GHResponse response = graphHopper.route(ghRequest);

		if (response.hasErrors() || response.getAll().isEmpty()) {
			logger.info("Could not find solution for edge with ID & Start: " + edge.id + " - "
					+ edge.getStart().toString());
			return null;
		}
		PathWrapper path = response.getBest();
		PointList points = path.getPoints();

		HighResEdge highResEdge = new HighResEdge(edge);
		highResEdge.addGhPositions(points.toGeoJson());
		return highResEdge;
	}

	public void reducePointCount(RouteController routeController, int factor) {
		logger.info("Reduce point resolution per edge; before - after");
		routeController.sumRoutePoints();

		List<List<Edge>> routes = routeController.getAllRoutes();
		for (int i = 0; i < routes.size(); i++) {
			List<Edge> edges = routes.get(i);
			if (!edges.isEmpty() && edges.get(0) instanceof MapRoute) {
				for (Edge edge : edges) {
					MapRoute mapEdge = (MapRoute) edge;
					routeController.updateEdge(mapEdge, reduceEdgePoints(mapEdge, factor), i);
				}
			} else {
				for (Edge edge : edges) {
					routeController.updateEdge(edge, reduceEdgePoints(edge, factor), i);
				}
			}
		}
		// repaint();
		routeController.sumRoutePoints();
	}

	public void reducePointCount(RouteController routeController) {
		reducePointCount(routeController, 1);
	}

	/**
	 * Checks if an {@link Edge} has multiple nearby points and only leaves one
	 * point at this position.
	 * 
	 * Create new Edge with old parameters. Iterate trough all points from the
	 * old edge but add the current point only if it do not have any near points
	 * in the new savedPoint list.
	 * 
	 * @param edge
	 * @return
	 */
	private Edge reduceEdgePoints(Edge refEdge, int factor) {
		HighResEdge edge = new HighResEdge(refEdge);
		for (GeoPosition point : refEdge.getPositions()) {
			boolean flag = true;
			for (GeoPosition savedPoint : edge.getPositions()) {
				if (isNear(point, savedPoint, factor * reduceEdgeDistance)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				edge.getPositions().add(point);
			}
		}
		return edge;
	}

	private Edge reduceEdgePoints(MapRoute mapedge, int factor) {
		MapRoute edge = new MapRoute(mapedge);
		for (MapPoint point : mapedge.getPoints()) {
			boolean flag = true;
			for (GeoPosition savedPoint : edge.getPositions()) {
				if (isNear(point.getPosition(), savedPoint, factor * reduceEdgeDistance)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				edge.addPoint(point);
			}
		}
		return edge;
	}

	/**
	 * Returns the nearest {@link MapNode} of the given {@link List} within a
	 * specific radius.
	 * 
	 * @param point
	 * @param nodes
	 *            {@link List} of {@link MapNode}s to search in
	 * @return nearest {@link MapNode} if within radius, <br>
	 *         <code>null</code> otherwise
	 */
	private MapNode getNearestNode(GeoPosition point, List<MapNode> nodes) {
		for (MapNode mapNode : nodes) {
			if (isNear(mapNode.getPosition(), point, MainController.combinePointsDistance)) {
				return mapNode;
			}
		}
		return null;
	}

	private static CapacityWaypoint getNearWaypoint(GeoPosition point, List<CapacityWaypoint> nodes, double distance) {
		for (CapacityWaypoint waypoint : nodes) {
			if (isNear(waypoint.getPosition(), point, distance)) {
				return waypoint;
			}
		}
		return null;
	}

	/**
	 * @param point
	 * @return
	 */
	public static Map<Edge, GeoPosition> getNearEdges(GeoPosition refPoint, List<Edge> edges, double distance) {
		Map<Edge, GeoPosition> nearEdges = new HashMap<>();

		for (int i = 0; i < edges.size(); i++) {
			for (GeoPosition point : edges.get(i).getPositions()) {
				if (isNear(refPoint, point, distance)) {
					nearEdges.put(edges.get(i), point);
					continue;
				}
			}
		}
		return nearEdges;
	}

	public static boolean isNear(GeoPosition refPoint, GeoPosition point, double distance) {
		if (getDistance(refPoint, point) < distance) {
			return true;
		} else {
			return false;
		}
	}

	public static double getDistance(GeoPosition refPoint, GeoPosition point) {
		double deltaX = Math.abs(refPoint.getLatitude() - point.getLatitude());
		double deltaY = Math.abs(refPoint.getLongitude() - point.getLongitude());
		return deltaX + deltaY;
	}

}
