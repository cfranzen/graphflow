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

import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.MainController;
import main.RouteController;
import models.Edge;
import newVersion.models.MapNode;
import newVersion.models.NodeEdge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Optimizer {

	private static final Logger logger = LoggerFactory.getLogger(Optimizer.class);

	private RouteController routeController;

	/**
	 * 
	 */
	public Optimizer() {
		this.routeController = RouteController.getInstance();
	}

	public void optimize() {
		routeController.sumAllPoints();
		logger.info("optimize v2");
		// combine edges & create almost empty map points
		// Creats MapNodes foreach simple point in the list
		List<Edge> edges = routeController.getRoute();
		List<Edge> savedEdges = new ArrayList<>();
		List<MapNode> savedNodes = new ArrayList<>();
		for (int i = 0; i < edges.size(); i++) {
			Edge currentEdge = edges.get(i);
			NodeEdge mapEdge = new NodeEdge(currentEdge);
			for (GeoPosition point : currentEdge.getPositions()) {
				MapNode node = getNearestNode(point, savedNodes);
				if (node == null) {
					node = new MapNode(point);
					savedNodes.add(node);
				}
				node.addEdge(currentEdge);
				mapEdge.points.add(node);
			}
			savedEdges.add(mapEdge);
			logger.info("Edge " + i + " from " + edges.size() + " processed");
		}

		logger.info("Precalculate Capacity and Workload");
		ExecutorService service = Executors.newCachedThreadPool();
		// for each map point, calc work&cap
		final int timesteps = edges.get(0).getCapacites().length;
		List<Future<?>> futures = new ArrayList<>();
		for (MapNode mapNode : savedNodes) {
			futures.add(service.submit(() -> {
				mapNode.calcCapacityAndWorkload(timesteps);
			}));
		}
		logger.info("Futures generated, wait for completition");
		for (int i = 0; i < futures.size(); i++) {
			Future<?> future = futures.get(i);
			try {
				future.get();
				if (i % 100 == 0) {
					logger.info("Computed CapWork for Point " + i + " from " + futures.size());
				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		routeController.setRoute(savedEdges);
		PaintController.flagNew = true; // TODO Refactor
		
		logger.info("optimize v2-end");
	}

	/**
	 * @param point
	 * @param savedEdges
	 * @return
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
