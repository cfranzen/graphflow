package painter;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class PathElement {

	public GeoPosition start;
	public GeoPosition end;

	public CurveType type;
	public GeoPosition[] additionalPoints;

	/**
	 * Creates a new {@link PathElement} which represents a simple line between
	 * start and end.
	 * 
	 * @param start
	 *            line start point
	 * @param end
	 *            line end point
	 */
	public PathElement(GeoPosition start, GeoPosition end) {
		this.start = start;
		this.end = end;
		this.type = CurveType.LINE;
	}

	/**
	 * Creates a new {@link PathElement} which represents a quadratic curve
	 * between start and end.
	 * 
	 * @param start
	 *            line start point
	 * @param end
	 *            line end point
	 * @param quadPoint
	 * 				
	 */
	public PathElement(GeoPosition start, GeoPosition end, GeoPosition quadPoint) {
		this.start = start;
		this.end = end;
		this.additionalPoints = new GeoPosition[]{quadPoint};
		this.type = CurveType.QUAD;
	}
	
	/**
	 * Creates a new {@link PathElement} with the given {@link CurveType}
	 * between start and end.
	 * 
	 * @param start
	 *            line start point
	 * @param end
	 *            line end point
	 * @param quadPoint
	 * 				
	 */
	public PathElement(GeoPosition start, GeoPosition end, GeoPosition[] additionalPoints, CurveType type) {
		this.start = start;
		this.end = end;
		this.additionalPoints = additionalPoints;
		this.type = type;
	}

}
