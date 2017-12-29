package sea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncrotess.pathfinder.model.entity.ServiceType;

import gui.MyMap;
import models.Edge;
import painter.SeaRoutePainter;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaController {

	private static final Logger logger = LoggerFactory.getLogger(SeaController.class);

	private static final Pattern PATTERN = Pattern.compile("\\[id=(\\w+),pos=\\[([\\w+,.,-]+), ([\\w+,.,-]+)\\],edges=\\[(\\w+(, \\w+)*)\\]\\]");

	private static final String DEFAULT_SEA_NODES_FILE = "src/main/resources/seaNodes.txt";
	
	private static SeaController instance;

	private List<SeaNode> points = new ArrayList<>();
	private List<Edge> edges = new ArrayList<>(); // represents the ways on the sea
	
	private int idCounter = 0;

	
	private SeaRoutePainter seaRoutePainter; // TODO move used methods in this class
	
	private SeaController(MyMap map) {
		seaRoutePainter = new SeaRoutePainter(map);
	}
	
	/**
	 * @return
	 */
	public static SeaController getInstance(MyMap map) {
		if (instance == null) {
			instance = new SeaController(map);
		}
		return instance;
	}
	
	public List<SeaNode> addEdgesFromFile(String filepath) {
		try {
			return Files.lines(Paths.get(filepath))
				.map(SeaNode::new)
				.collect(Collectors.toList());
		} catch (IOException e) {
			File f = new File(filepath);
			logger.error("Absolute filepath invalid: " + f.getAbsolutePath());
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * Creates a new {@link SeaNode} at the given coordinates and adds it to the controllers {@link List}.
	 * 
	 * @param coordsForMouse
	 */
	public void addNewNode(GeoPosition coordsForMouse) {
		
		SeaNode node = createPointFromPos(coordsForMouse);
		if (points.size() > 1) {
			node.addEdge(node.id-1);
			points.get((int) (node.id-1)).edges.add(node.id);
		}
		logger.info("Added node: " + node.toString());
		edges = createEdges();
	}
	
	public SeaNode createPointFromPos(GeoPosition pos) {
		return createPointFromPos(pos, new ArrayList<>());
	}
	
	public SeaNode createPointFromPos(GeoPosition pos, List<Long> edges) {
		SeaNode point = new SeaNode(idCounter++, pos, edges);
		points.add(point);
		return point;
	}
	
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public List<Edge> createEdges() {
		List<Edge> result = new ArrayList<>();
//		for (SeaNode seaNode : points) {
		for (int i = 0; i < points.size(); i++) {
			SeaNode seaNode = points.get(i);
			for (Long pointref : seaNode.edges) {
				SeaNode des = getPosById(pointref);
				GeoPosition from = seaNode.pos;
//				while (Edge.calcDistance(from, des.pos) > Constants.SEA_NODE_MAX_DISTANCE) {
//					GeoPosition pos = SeaRoutePainter.getCirclePointAsGeo(from, des.pos, SeaRoutePainter.circleDiameterFaktor(0.6));
//					createPointFromPos(pos,);
////					System.out.println(from);
////					System.out.println(des.pos);
////					System.out.println(pos);
////					System.out.println("");
//					Edge edge = new Edge(from, pos);
//					edge.setInfo(pointref + "");
//					edge.setType(ServiceType.UNKOWN);
//					from = pos;
//				}
				
				Edge edge = new Edge(from, des.pos);
				edge.setInfo(pointref + "");
				edge.setType(ServiceType.UNKOWN);
				
				result.add(edge);
			}
			
//			logger.info("Processed SeaNode " + i + " from " + points.size());
		}
		
		return result;
		
	}
	
	private SeaNode getPosById(long id) {
		SeaNode result = points.stream()
		        .filter(node -> node.id == id)
		        .findAny()
		        .orElse(null);
		if (result != null) {
			return result;
		} 
		return null;
	}
	
	public void printNodes() {
		for (SeaNode seaNode : points) {
			// Sys out for easier copy
			System.out.println(seaNode);
			
		}
		
	}
	
	/**
	 * @param seaNodes
	 */
	public void loadSeaNodes(String seaNodes) {
		String path = seaNodes;
		if (null == path || "".equals(path)) {
			logger.info("use default seanodes path");
			path = DEFAULT_SEA_NODES_FILE;
		}
		points = addEdgesFromFile(path);
		idCounter = points.size(); // Works only if size = latest id
		logger.info("Set IDCounter to: " + idCounter);
		edges = createEdges();
		logger.info(edges.toString());
	}

	public class SeaNode {
		
		public long id;
		public GeoPosition pos;
		public List<Long> edges = new ArrayList<>();
		
		
		/**
		 * @param iD
		 * @param pos
		 */
		public SeaNode(long id, GeoPosition pos, List<Long> edges) {
			super();
			this.id = id;
			this.pos = pos;
			this.edges = edges;
		}

		public SeaNode(String line) {
			final Matcher matcher = PATTERN.matcher(line);

			logger.debug("Parsing line: " + line);
			
			if (matcher.find()) {
			    this.id = Long.parseLong(matcher.group(1));
			    this.pos = getValidGeoPos(Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)));
			    this.edges = Arrays.stream(matcher.group(4).split(","))
			    		.map(String :: trim)
			            .map((x) -> Long.parseLong(x))
			            .collect(Collectors.toList());
			} else {
				logger.warn("No match found for line: " + line);
			}
		}
		
		private GeoPosition getValidGeoPos(double lat, double lon) {
			double vLon = lon;
			if (lon > 180) {
				vLon = -180 + (lon - 180); 
			} else if (lon < -180) {
				vLon = 180 - (lon + 180);
			}
			return new GeoPosition(lat, vLon);
			
		}
		
		public void addEdge(long nodeId) {
			edges.add(nodeId);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
		
	}

}
