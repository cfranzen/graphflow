package sea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncrotess.pathfinder.model.entity.ServiceType;

import models.Edge;
import newVersion.models.SeaNode;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public final class SeaNodeFactory {

	private static final Logger logger = LoggerFactory.getLogger(SeaNodeFactory.class);
	private static final String DEFAULT_SEA_NODES_FILE = "src/main/resources/seaNodes.txt";

	private static List<SeaNode> points = new ArrayList<>();
	private static List<Edge> edges = new ArrayList<>(); // represents the ways
															// on the
	private static int idCounter = 0;

	/**
	 * Creates a new {@link SeaNode} at the given coordinates and adds it to the
	 * controllers {@link List}. For initial creation of seanodes
	 * 
	 * @param coordsForMouse
	 */
	private void addNewNode(GeoPosition coordsForMouse) {
		SeaNode node = createPointFromPos(coordsForMouse);
		if (points.size() > 1) {
			node.addEdge(node.id - 1);
			points.get((int) (node.id - 1)).edges.add(node.id);
		}
		logger.info("Added node: " + node.toString());
		edges = createEdges();
	}

	private SeaNode createPointFromPos(GeoPosition pos) {
		return createPointFromPos(pos, new ArrayList<>());
	}

	private SeaNode createPointFromPos(GeoPosition pos, List<Long> edges) {
		SeaNode point = new SeaNode(idCounter++, pos, edges);
		points.add(point);
		return point;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	private static List<Edge> createEdges() {
		List<Edge> result = new ArrayList<>();
		// for (SeaNode seaNode : points) {
		for (int i = 0; i < points.size(); i++) {
			SeaNode seaNode = points.get(i);
			for (Long pointref : seaNode.edges) {
				SeaNode des = getPosById(pointref);
				GeoPosition from = seaNode.pos;

				Edge edge = new Edge(from, des.pos);
				edge.setInfo(pointref + "");
				edge.setType(ServiceType.UNKOWN);

				result.add(edge);
			}

			// logger.info("Processed SeaNode " + i + " from " + points.size());
		}

		return result;

	}

	private static SeaNode getPosById(long id) {
		SeaNode result = points.stream().filter(node -> node.id == id).findAny().orElse(null);
		if (result != null) {
			return result;
		}
		return null;
	}

	/**
	 * @param seaNodes
	 */
	public static List<Edge> loadSeaNodes(String seaNodes) {
		String path = seaNodes;
		if (null == path || "".equals(path)) {
			logger.warn("no sea nodes file given, try to use default sea nodes path...");
			path = DEFAULT_SEA_NODES_FILE;
		}
		points = addEdgesFromFile(path);
		idCounter = points.size(); // Works only if size = latest id
		logger.debug("Set IDCounter to: " + idCounter);
		edges = createEdges();
		return edges;
	}

	private static List<SeaNode> addEdgesFromFile(String filepath) {
		try {
			return Files.lines(Paths.get(filepath)).map(SeaNode::new).collect(Collectors.toList());
		} catch (IOException e) {
			File f = new File(filepath);
			logger.error("Absolute filepath invalid: " + f.getAbsolutePath());
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
