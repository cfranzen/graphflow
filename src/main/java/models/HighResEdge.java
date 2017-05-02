package models;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class HighResEdge extends Edge {

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
	}

	@Override
	public List<GeoPosition> getPositions() {
		return points;
	}

	/**
	 * 
	 * @param doubles
	 */
	public void addGhPosition(Double[] point) {
		points.add(new GeoPosition(point[1], point[0]));
	}

	/**
	 * Adds a {@link GeoPosition} with the given coordinates.
	 * 
	 * @param point
	 *            double-array with global coordinates
	 */
	public void addPosition(Double[] point) {
		points.add(new GeoPosition(point[1], point[0]));
	}

	/**
	 * Adds all {@link GeoPosition}s of the given {@link List}.
	 * 
	 * @param oldList
	 *            {@link List} to add.
	 */
	public void addPositions(List<GeoPosition> oldList) {
		points.addAll(oldList);
	}

	/**
	 * Returns the {@link GeoPosition} with the given index.
	 * 
	 * @param i
	 *            index
	 * @return {@link GeoPosition} to the given index.
	 */
	public GeoPosition getPosition(int i) {
		if (i < 0) {
			return points.get(points.size() + i);
		} else {
			return points.get(i);
		}
	}

}
