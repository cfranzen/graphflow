/**
 * 
 */
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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;
import com.syncrotess.pathfinder.model.entity.ServiceType;

import models.Edge;

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
	
	private int idCounter = 0;
	
	private SeaController() {
		// noop
	}
	
	/**
	 * @return
	 */
	public static SeaController getInstance() {
		if (instance == null) {
			instance = new SeaController();
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
	}
	
	public SeaNode createPointFromPos(GeoPosition pos) {
		return createPointFromPos(pos, new ArrayList<>());
	}
	
	public SeaNode createPointFromPos(GeoPosition pos, List<Long> edges) {
		SeaNode point = new SeaNode(idCounter++, pos, edges);
		points.add(point);
		return point;
	}
	
	public List<Edge> createEdges() {
		List<Edge> result = new ArrayList<>();
		for (SeaNode seaNode : points) {
			for (Long pointref : seaNode.edges) {
				if (pointref < seaNode.id) {
					Edge edge = new Edge(seaNode.pos, getPosById(pointref));
					edge.setInfo(pointref + "");
					edge.setType(ServiceType.VESSEL_TRANSPORT);
					result.add(edge);
//					logger.info(edge.toString());
				}
			}
		}
		return result;
		
	}
	
	private GeoPosition getPosById(long id) {
		SeaNode result = points.stream()
		        .filter(node -> node.id == id)
		        .findAny()
		        .orElse(null);
		if (result != null) {
			return result.pos;
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
	}

	public void initGraphhopperSea(String ghConfigFileFolder) {
		GraphHopper graphHopper = new GraphHopperOSM().forDesktop();
		
		CarFlagEncoder encoder = new CarFlagEncoder();
		graphHopper.setEncodingManager(new EncodingManager(encoder));
		graphHopper.getCHFactoryDecorator().setEnabled(false);

		try {
			CmdArgs args = CmdArgs.readFromConfig(ghConfigFileFolder + "config.properties", "graphhopper.config");
//			args.put("datareader.file", cliInput.osmFilePath);
//			args.put("graph.location", cliInput.ghFolder);
			graphHopper.init(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		graphHopper.importOrLoad();
		
//		graphHopper.getGraphHopperStorage().set
		
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
