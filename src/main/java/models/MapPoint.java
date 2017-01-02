package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapPoint {

	public double x;
	public double y;
	public Map<Edge, Double[]> edgeMap;
	public Double[] contactPoint = null;
	
	/**
	 * @param x
	 * @param y
	 * @param edges
	 */
	public MapPoint(double x, double y, Map<Edge, Double[]> edgeMap) {
		super();
		this.x = x;
		this.y = y;
		this.edgeMap = edgeMap;
	}

	/**
	 * @param point
	 * @param nearEdges
	 */
	public MapPoint(Double[] point, Map<Edge, Double[]> edgeMap) {
		super();
		this.x = point[0];
		this.y = point[1];
		this.edgeMap = edgeMap;
		
	}
	
}
