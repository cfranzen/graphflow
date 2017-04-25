package painter;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;

import models.Constants;

/**
 * This class uses multiple {@link PointPath}-objects to store the sea-ways. The
 * Class takes {@link GeoPosition}s to identify the {@link Path2D} independent
 * of the active zoom level of the View.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class GeoPath {

	private PointPath[] pathes = new PointPath[Constants.MAX_ZOOM_LEVEL];
	private List<PathElement> elements = new ArrayList<>();

	private TileFactory factory;

	
	public GeoPath() {
		// TODO Auto-generated constructor stub
	}

	public GeoPath(PointPath path) {
		for (int i = 0; i < pathes.length; i++) {
			pathes[i] = path;
		}
	}
	
	/**
	 * 
	 */
	public void addCurve(GeoPosition start, GeoPosition end, GeoPosition quadPoint) {
		elements.add(new PathElement(start, end, quadPoint));
	}

	public void addLine(GeoPosition start, GeoPosition end) {
		elements.add(new PathElement(start, end));
	}

	public void calcPixelPaths(TileFactory factory) {
		this.factory = factory;
		for (int i = 0; i < pathes.length; i++) {
			PointPath path = calcPath(i);
			pathes[i] = path;
		}
	}

	public PointPath getPath(int zoom) {
		zoom--;
		if (zoom >= pathes.length) {
			return null;
		}
		if (pathes[zoom] == null) {
			return calcPath(zoom);
		} else {
			return pathes[zoom];
		}
	}
	
	private PointPath calcPath(int zoom) {
		PointPath path = new PointPath();
		GeoPosition firstpos = elements.get(0).start;
		path.moveTo(factory.geoToPixel(firstpos, zoom));
		for (PathElement pathElement : elements) {
			switch (pathElement.type) {
			case QUAD:
				path.quadTo(factory.geoToPixel(pathElement.additionalPoints[0], zoom),
						factory.geoToPixel(pathElement.end, zoom));
				break;
			case LINE:
			default:
				path.lineTo(factory.geoToPixel(pathElement.end, zoom));
			}
		}
		return path;
	}

	
	
}
