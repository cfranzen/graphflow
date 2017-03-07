package gui;

import java.util.ArrayList;
import java.util.List;

import models.Edge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RouteController {

	private List<Edge> route = new ArrayList<>();
	
	private IRoutePainter defaultPainter = new DefaultRoutePainter();
	
	/**
	 * @return the route
	 */
	public List<Edge> getRoute() {
		return route;
	}


	/**
	 * @param route the route to set
	 */
	public void setRoute(List<Edge> route) {
		defaultPainter.setRoute(route);
		this.route = route;
	}


	public IRoutePainter getRoutePainter(int zoomlevel) {
		switch (zoomlevel) {
		case 1:
		default:
			return defaultPainter;
		}
	}

}
