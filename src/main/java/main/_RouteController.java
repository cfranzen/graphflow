/**
 * 
 */
package main;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import models.Edge;
import models.HighResEdge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public interface _RouteController {

	/**
	 * @return the route
	 */
	List<Edge> getRoute();

	/**
	 * {@link List} with {@link HighResEdge} after mapping
	 * 
	 * @return the route
	 */

	List<Edge> getPaintRoute();

	/**
	 * @param route
	 *            the route to set
	 */
	void setRoute(List<Edge> route);

	/**
	 * 
	 * @param viewportStart
	 *            {@link GeoPosition} at pixel (1,1) of the viewport
	 * @param viewportEnd
	 *            {@link GeoPosition} at last bottom-right pixel of the viewport
	 */
	void refreshPaintRoutes(GeoPosition viewportStart, GeoPosition viewportEnd);

	/**
	 * 
	 */
	void sumAllPoints();

}