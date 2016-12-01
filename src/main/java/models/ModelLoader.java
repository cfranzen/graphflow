package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.IOUtils;
import org.jxmapviewer.viewer.GeoPosition;

import com.syncrotess.pathfinder.Constants;
import com.syncrotess.pathfinder.model.entity.EntityFactory;
import com.syncrotess.pathfinder.model.entity.Model;
import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;
import com.syncrotess.pathfinder.model.entity.impl.EntityFactoryImpl;
import com.syncrotess.pathfinder.model.parser.ModelParser;
import com.syncrotess.pathfinder.model.parser.StringTokenModelParser;
import com.syncrotess.pathfinder.model.reader.ModelReader;
import com.syncrotess.pathfinder.model.reader.ReaderUtils;
import com.syncrotess.pathfinder.model.reader.TextFileModelReader;
import com.syncrotess.pathfinder.solution.Solution;
import com.syncrotess.pathfinder.solution.SolutionFactory;
import com.syncrotess.pathfinder.solution.SolutionItem;
import com.syncrotess.pathfinder.solution.impl.SolutionFactoryImpl;
import com.syncrotess.pathfinder.solution.io.TextFileSolutionReader;
import com.syncrotess.pathfinder.util.tokenizer.CharacterStreamTokenizer;

public class ModelLoader {

	/**
	 * File path to the model which should be represented.
	 */
	private static final String FILE_PATH_MODEL = "src/main/resources/examples/testModel.txt.gz";

	/**
	 * File path to the solution for the given model.
	 */
	private static final String FILE_PATH_SOLUTION = "src/main/resources/examples/testModelSolution.sol";
	public static boolean printDebug = false;

	public static void main(final String[] args) {
		loadFile();
	}

	public static ModelLoader loadFile() {
		Model model = loadModel();
		Solution solution = loadSolution(model);

		SortedSet<Node> allNodes = model.getServiceNetwork().getNodes();
		SortedSet<Service> allServices = model.getServiceNetwork().getServices();

		List<GeoPosition> nodes = createNodes(allNodes);
		int timesteps = model.getPlanningHorizon().getLength();
		List<Edge> edges = createEdges(timesteps, allServices, solution);

		debugOut(allNodes, allServices);
		return new ModelLoader(nodes, edges, timesteps);
	}

	private static Solution loadSolution(Model model) {
		FileInputStream is;
		Solution solution = null;
		try {
			is = new FileInputStream(FILE_PATH_SOLUTION);
			final InputStreamReader solutionFileReader = new InputStreamReader(is, Constants.DEFAULT_CHARSET);
			final CharacterStreamTokenizer solutionTokenizer = new CharacterStreamTokenizer(solutionFileReader);
			final SolutionFactory solutionFactory = new SolutionFactoryImpl();
			final TextFileSolutionReader solutionReader = new TextFileSolutionReader(solutionTokenizer, solutionFactory,
					model);
			solution = solutionReader.readSolution();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return solution;
	}

	private static List<GeoPosition> createNodes(SortedSet<Node> allNodes) {
		List<GeoPosition> nodes = new ArrayList<>();
		for (Node node : allNodes) {
			nodes.add(new GeoPosition(node.getLatitude(), node.getLongitude()));
		}
		return nodes;
	}

	/**
	 * Creates {@link Edge}s from the given {@link Service}s.
	 * 
	 * @param model
	 * @param allServices
	 * @param solution
	 * @return
	 */
	private static List<Edge> createEdges(int timesteps, SortedSet<Service> allServices, final Solution solution) {
		List<Edge> allEdges = new ArrayList<>();
		for (Service service : allServices) {
			Node start = service.getStartNode();
			Node end = service.getEndNode();

			// Position
			Edge edge = new Edge(new GeoPosition(start.getLatitude(), start.getLongitude()),
					new GeoPosition(end.getLatitude(), end.getLongitude()));

			// Workloads
			int[] workloads = new int[timesteps];
			Set<SolutionItem> items = solution.getItemsForService(service);
			for (SolutionItem solutionItem : items) {
				workloads[solutionItem.getTime()] += solutionItem.getVolume();
			}
			edge.setWorkload(workloads);

			// Capacities
			int[] capacites = new int[timesteps];
			for (int i = 0; i < timesteps; i++) {
				capacites[i] = service.getCapacity(i);
			}
			edge.setCapacites(capacites);

			// Other
			edge.setType(service.getServiceType());

			allEdges.add(edge);
		}
		return allEdges;
	}

	private static void debugOut(SortedSet<Node> allNodes, SortedSet<Service> allEdges) {
		if (printDebug) {
			System.out.println("All nodes of network:");
			for (Node node : allNodes) {
				System.out.println(node.getName() + " [" + node.getLatitude() + ", " + node.getLongitude() + "]");
			}

			System.out.println("All edges of network:");
			for (Service edge : allEdges) {
				System.out.println(edge.getStartNode().getName() + "->" + edge.getEndNode().getName() + " - Capacity: "
						+ edge.getCapacities());
			}
		}
	}

	private static Model loadModel() {
		File modelFile = new File(FILE_PATH_MODEL);

		Reader fileReader = null;
		Model model = null;
		try {
			fileReader = ReaderUtils.getReaderForFile(modelFile, Constants.DEFAULT_CHARSET);
			final CharacterStreamTokenizer tokenizer = new CharacterStreamTokenizer(fileReader);
			final ModelReader modelReader = new TextFileModelReader(tokenizer);
			final EntityFactory entityFactory = new EntityFactoryImpl();
			final ModelParser modelParser = new StringTokenModelParser(modelReader, entityFactory);
			model = modelParser.parse();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileReader);
		}
		return model;
	}

	/**
	 * Creates a new {@link ModelLoader}-object.
	 * 
	 * @param nodes
	 * @param edges
	 * @param timesteps
	 */
	public ModelLoader(List<GeoPosition> nodes, List<Edge> edges, int timesteps) {
		super();
		this.nodes = nodes;
		this.edges = edges;
		this.timesteps = timesteps;
	}

	public List<GeoPosition> nodes;
	public List<Edge> edges;
	public int timesteps;

}
