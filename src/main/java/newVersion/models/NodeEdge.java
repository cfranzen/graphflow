package newVersion.models;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
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

	public List<MapNode> nodes = new ArrayList<>();

	/**
	 * Contains one {@link Path2D}-Object for every zoom level.
	 */
	private List<Path2D> currentZoomPath = new ArrayList<>();
	private int pathZoomLevel = 0;

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
		return nodes;
	}

	public MapNode get(int i) {
		return nodes.get(i);
	}

	private Path2D calculateShape(MyMap map) {
		Path2D.Double path = new Path2D.Double();
		Point2D p = map.getTileFactory().geoToPixel(start, map.getZoom());
		path.moveTo(p.getX(), p.getY());
		for (MapNode node : nodes) {
			p = map.getTileFactory().geoToPixel(node.getPosition(), map.getZoom());
			path.lineTo(p.getX(), p.getY());
		}
		p = map.getTileFactory().geoToPixel(dest, map.getZoom());
		path.lineTo(p.getX(), p.getY());
		return path;
	}

	/**
	 * @return the {@link Shape} of the whole edge
	 */
	public Path2D getShape(MyMap map) {
		// Generates a new path on zoom level change
		if (currentZoomPath.isEmpty() || map.getZoom() != pathZoomLevel) {
			currentZoomPath.clear();
			currentZoomPath.add(calculateShape(map));
			pathZoomLevel = map.getZoom();
		}
		Path2D result = new Path2D.Double();
//		currentZoomPath.stream().forEach(p -> result.append(p, false));
		for (int i = 0; i < currentZoomPath.size(); i++) {
			result.append(currentZoomPath.get(i), true);
		}
		return result;
	}

	/**
	 * @param j
	 * @return
	 */
	public GeoPosition getPosition(int j) {
		if (j < 0) {
			return nodes.get(nodes.size() + j).getPosition();
		}
		j %= nodes.size();
		return nodes.get(j).getPosition();
	}

	/**
	 * @param map
	 * @return
	 */
	public List<Path2D> getPath() {
		return currentZoomPath;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(List<Path2D> path, int zoomLevel) {
		this.currentZoomPath = path;
		this.pathZoomLevel = zoomLevel;
	}

	public void addToPath(List<Path2D> path) {
		this.currentZoomPath.addAll(path);
	}

	public void addToPath(Path2D path) {
		this.currentZoomPath.add(path);
	}

	public void setPathZoom(int zoomLevel) {
		this.pathZoomLevel = zoomLevel;
	}

	/**
	 * @return
	 */
	public double getPathSize() {
		if (currentZoomPath.size() == 1) {
			return nodes.size();
		}
		return currentZoomPath.size();
	}

}
