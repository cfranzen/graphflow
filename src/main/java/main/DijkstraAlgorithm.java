package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.util.DistanceCalc3D;

import models.Edge;

public class DijkstraAlgorithm {

	private static final DistanceCalc3D calulator = new DistanceCalc3D();
	
	private final List<Edge> edges;
	private HashSet<GeoPosition> allNodes = new HashSet<>();
	private Set<GeoPosition> settledNodes;
	private Set<GeoPosition> unSettledNodes;
	private Map<GeoPosition, GeoPosition> predecessors;
	private Map<GeoPosition, Double> distance;

	public DijkstraAlgorithm(List<Edge> edges) {
		// create a copy of the array so that we can operate on this array
		this.edges = new ArrayList<Edge>(edges);
		for (Edge edge : edges) {
			allNodes.add(edge.getStart());
			allNodes.add(edge.getDest());
		}
	}

	public void execute(GeoPosition source) {
		
		source = findNearestGraphNode(source);
		
		settledNodes = new HashSet<GeoPosition>();
		unSettledNodes = new HashSet<GeoPosition>();
		distance = new HashMap<GeoPosition, Double>();
		predecessors = new HashMap<GeoPosition, GeoPosition>();
		distance.put(source, 0.);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			GeoPosition node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(GeoPosition node) {
		List<GeoPosition> adjacentNodes = getNeighbors(node);
		for (GeoPosition target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
				distance.put(target, getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}
	}

	private double getDistance(GeoPosition node, GeoPosition target) {
		for (Edge edge : edges) {
			if (edge.getStart().equals(node) && edge.getDest().equals(target)) {
				return edge.getDistance();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<GeoPosition> getNeighbors(GeoPosition node) {
		List<GeoPosition> neighbors = new ArrayList<GeoPosition>();
		for (Edge edge : edges) {
			if (edge.getStart().equals(node) && !isSettled(edge.getDest())) {
				neighbors.add(edge.getDest());
			}
		}
		return neighbors;
	}

	private GeoPosition getMinimum(Set<GeoPosition> vertexes) {
		GeoPosition minimum = null;
		for (GeoPosition vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(GeoPosition vertex) {
		return settledNodes.contains(vertex);
	}

	private double getShortestDistance(GeoPosition destination) {
		Double d = distance.get(destination);
		if (d == null) {
			return Double.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public LinkedList<GeoPosition> getPath(GeoPosition target) {
		
		target = findNearestGraphNode(target);
		
		LinkedList<GeoPosition> path = new LinkedList<GeoPosition>();
		GeoPosition step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

	/**
	 * @param target
	 * @return
	 */
	public GeoPosition findNearestGraphNode(GeoPosition target) {
		GeoPosition minNode = null;
		double minDist = Double.MAX_VALUE;
		for (GeoPosition geoPosition : allNodes) {
			double curDist = calulator.calcDist(target.getLatitude(), target.getLongitude(), geoPosition.getLatitude(), geoPosition.getLongitude());
			if (minDist > curDist) {
				minDist = curDist;
				minNode = geoPosition;
			}
					
		}
		return minNode;
	}

}