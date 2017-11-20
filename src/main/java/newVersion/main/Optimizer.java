/**
 * 
 */
package newVersion.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.MainController;
import main.RouteController;
import models.Constants;
import models.Edge;
import newVersion.models.MapNode;
import newVersion.models.NodeEdge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Optimizer {

	private static final Logger logger = LoggerFactory.getLogger(Optimizer.class);

	/**
	 * @param routeController
	 * 
	 */
	public Optimizer() {
		// noop
	}

	public void optimize(RouteController routeController, WaypointController waypointController) {
		routeController.sumAllPoints();
		logger.info("optimize v2");

		aggregateWaypoints(routeController, waypointController);

		// combine edges & create almost empty map points
		// Creates MapNodes for each simple point in the list
		if (Constants.optimzeLandRoutes) {
			routeController.setRoute(optimizeGivenEdges(routeController.getRoute()));
			logger.info("land edges done - now processing sea edges");
		}

		List<Edge> seaRoute = optimizeGivenEdges(routeController.getSeaRoute());
		routeController.setSeaRoute(seaRoute);
		PaintController.useNewPainter(); // TODO Refactor

		routeController.sumAllPoints();
		logger.info("optimize v2-end");
	}

	/**
	 * @param routeController
	 * @param waypointController
	 */
	private void aggregateWaypoints(RouteController routeController, WaypointController waypointController) {
		long time = System.currentTimeMillis();
		logger.info("Aggregate waypoints");
		// TODO
		logger.info("Change route ends to aggregated waypoints");
		// TODO
		logger.info("Aggregate waypoints end - " + (System.currentTimeMillis() - time) + " ms");
	}

	private List<Edge> optimizeGivenEdges(List<Edge> edges) {
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
			savedEdges.add(mapEdge);
			logger.info("Edge " + i + " from " + edges.size() + " processed : " + mapEdge.nodes.size() + " Nodes - "
					+ (System.currentTimeMillis() - time) + " ms");
		}

		logger.info("Precalculate Capacity and Workload");
		ExecutorService service = // Executors.newCachedThreadPool();
				Executors.newFixedThreadPool(10);
		// for each map point, calc work&cap
		final int timesteps = edges.get(0).getCapacites().length;
		List<Future<?>> futures = new ArrayList<>();
		for (MapNode mapNode : savedNodes) {
			futures.add(service.submit(() -> {
				mapNode.calcCapacityAndWorkload(timesteps);
			}));
		}
		logger.info("Futures generated, wait for completition");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < futures.size(); i++) {
			Future<?> future = futures.get(i);
			try {

				future.get(200, TimeUnit.MILLISECONDS);
				if (i % 500 == 0) {
					logger.info("Computed CapWork for Point " + i + " from " + futures.size());
				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				logger.error("Future Timeout" + e.getMessage());
			}
		}
		return savedEdges;
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
			if (MainController.isNear(mapNode.getPosition(), point, MainController.combinePointsDistance)) {
				return mapNode;
			}
		}
		return null;
	}

}
