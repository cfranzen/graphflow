/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class HighResEdge extends Edge {

//	private List<Double[]> points = new ArrayList<>();
	private List<GeoPosition> points = new ArrayList<>();

	public HighResEdge() {
		super();
	}

	/**
	 * @param start
	 * @param dest
	 */
	public HighResEdge(GeoPosition start, GeoPosition dest) {
		super(start, dest);
	}

	/**
	 * @param edge
	 */
	public HighResEdge(Edge edge) {
		super(edge);
	}

	/**
	 * @param geoJson
	 *            is LON,LAT or LON,LAT,ELE
	 * 
	 */
	public void addGhPositions(List<Double[]> geoJson) {
		for (Double[] point : geoJson) {
			addGhPosition(point);
		}

		// points.addAll(geoJson);

	}

	@Override
	public List<GeoPosition> getPositions() {
		return points;
	}

	/**
	 * @param doubles
	 */
	public void addGhPosition(Double[] point) {
		points.add(new GeoPosition( point[1], point[0]));
	}

	/**
	 * @param refPoint
	 */
	public void addPosition(Double[] refPoint) {
		points.add(new GeoPosition(refPoint[1], refPoint[0]));
	}

	/**
	 * @param oldList
	 */
	public void addPositions(List<GeoPosition> oldList) {
		points.addAll(oldList);
	}

}
