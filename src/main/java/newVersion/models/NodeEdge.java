package newVersion.models;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import models.Constants;
import models.Edge;

/**
 * Contains informations about which points are on this route/edge and the
 * service times from the entities.
 * <p>
 * Capacity and workload informations are found in the {@link MapNode}-class
 * 
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class NodeEdge extends Edge {

	public List<MapNode> points = new ArrayList<>();

	/**
	 * Contains one {@link Path2D}-Object for every zoom level.
	 */
	public Path2D.Double[] path = new Path2D.Double[Constants.MAX_ZOOM_LEVEL];

	/**
	 * @param currentEdge
	 */
	public NodeEdge(Edge e) {
		this.start = e.getStart();
		this.dest = e.getDest();
		this.serviceTimes = e.getServiceTimes();
	}

	/**
	 * @return
	 */
	public List<MapNode> getPoints() {
		return points;
	}

	public MapNode get(int i) {
		return points.get(i);
	}

	private Path2D.Double calculateShape(MyMap map) {
		Path2D.Double path = new Path2D.Double();
		Point2D p = map.getTileFactory().geoToPixel(start, map.getZoom());
		path.moveTo(p.getX(), p.getY());
		for (MapNode node : points) {
			p = map.getTileFactory().geoToPixel(node.getPosition(), map.getZoom());
			path.lineTo(p.getX(), p.getY());
		}
		return path;
	}

	/**
	 * @return the {@link Shape} of the whole edge
	 */
	public Path2D.Double getShape(MyMap map) {
		int zoom = map.getZoom() - 1;
		if (path[zoom] == null) {
			path[zoom] = calculateShape(map);
		}
		return path[zoom];
	}

	/**
	 * @param j
	 * @return
	 */
	public GeoPosition getPosition(int j) {
		if (j < 0) {
				return points.get(points.size() + j).getPosition();
		}
		j %= points.size();
		return points.get(j).getPosition();
	}

}
