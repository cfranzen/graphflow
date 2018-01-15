package dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import main.RouteController;
import models.Edge;
import models.HighResEdge;
import newVersion.models.NodeEdge;

/**
 * Special cloned class for storing and loading to disk
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RouteControllerDTO {
	public RouteControllerDTO() {
	}

	public RouteControllerDTO(RouteController routeController) {
		this.routes = routeController.getAllRoutes().stream()
				.map(e -> e.stream().map(n -> (NodeEdge) n).collect(Collectors.toList())).collect(Collectors.toList());
		this.highResEdgeMap = routeController.getHighResEdgeMap();
		this.seaRoute = routeController.getSeaRoute().stream().map(e -> (NodeEdge)e).collect(Collectors.toList());
	}

	public List<List<NodeEdge>> routes = new ArrayList<>();

	public HashMap<Integer, HighResEdge> highResEdgeMap;

	public List<NodeEdge> seaRoute = new ArrayList<>();

}
