package models;

import java.util.Map;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MapPoint {

	private GeoPosition posi;
	public Map<Edge, GeoPosition> edgeMap;
	public GeoPosition contactPoint = null;
	
	/**
	 * @param x
	 * @param y
	 * @param edges
	 */
	public MapPoint(double x, double y, Map<Edge, GeoPosition> edgeMap) {
		super();
		this.posi = new GeoPosition(x,y);
		this.edgeMap = edgeMap;
	}

	/**
	 * @param point
	 * @param nearEdges
	 */
	public MapPoint(GeoPosition point, Map<Edge, GeoPosition> edgeMap) {
		super();
		this.posi = point;
		this.edgeMap = edgeMap;
		
	}
	
	public GeoPosition getPosition() {
		return posi;
	}
	
}
