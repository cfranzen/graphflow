package models.pointBased;

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

	public static void main(final String[] args) {
		final String FILE_PATH_MODEL = "src/main/resources/examples/testModel.txt.gz";
		final String FILE_PATH_SOLUTION = "src/main/resources/examples/testModelSolution.sol";
		loadFile(FILE_PATH_MODEL, FILE_PATH_SOLUTION);
	}

	/**
	 * Parses the nodes and edges from the given files.
	 * 
	 * @param filePathModel
	 *            File path to the model which should be represented.
	 * @param filePathSolution
	 *            File path to the solution for the given model.
	 * @return {@link ModelLoader}-Object with parsed nodes and edges
	 */
	public static ModelLoader loadFile(String filePathModel, String filePathSolution) {
		Model model = loadModel(filePathModel);
		Solution solution = loadSolution(model, filePathSolution);

		SortedSet<Node> allNodes = model.getServiceNetwork().getNodes();
		SortedSet<Service> allServices = model.getServiceNetwork().getServices();
		
		List<GeoPosition> nodes = createNodes(allNodes);
		int timesteps = model.getPlanningHorizon().getLength();
		List<Edge> edges = createEdges(timesteps, allServices, solution);

		debugOut(allNodes, allServices);
		return new ModelLoader(nodes, edges, timesteps);
	}

	private static Solution loadSolution(Model model, String filePathSolution) {
		FileInputStream is;
		Solution solution = null;
		try {
			is = new FileInputStream(filePathSolution);
			final InputStreamReader solutionFileReader = new InputStreamReader(is, Constants.DEFAULT_CHARSET);
			final CharacterStreamTokenizer solutionTokenizer = new CharacterStreamTokenizer(solutionFileReader);
			final SolutionFactory solutionFactory = new SolutionFactoryImpl();
			final TextFileSolutionReader solutionReader = new TextFileSolutionReader(solutionTokenizer, solutionFactory,
					model);
			solution = solutionReader.readSolution();
		} catch (FileNotFoundException e) {
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
			long[] workloads = new long[timesteps];
			Set<SolutionItem> items = solution.getItemsForService(service);
			for (SolutionItem solutionItem : items) {
				workloads[solutionItem.getTime()] += solutionItem.getVolume();
			}
			edge.setWorkload(workloads);

			// Capacities
			long[] capacites = new long[timesteps];
			for (int i = 0; i < timesteps; i++) {
				capacites[i] = service.getCapacity(i);
			}
			edge.setCapacites(capacites);

			// Service Time
			int[] serviceTime = new int[timesteps];
			int maxServiceTime = 0;
			for (int i = 0; i < timesteps; i++) {
				serviceTime[i] = service.getServiceTime(i);
				if (serviceTime[i] > maxServiceTime) {
					maxServiceTime = serviceTime[i];
				}
			}
			edge.setServiceTime(serviceTime);
			edge.setMaxServiceTime(maxServiceTime);
			
			// Other
			edge.setType(service.getServiceType());

			allEdges.add(edge);
		}
		return allEdges;
	}

	private static void debugOut(SortedSet<Node> allNodes, SortedSet<Service> allEdges) {
		if (main.Constants.debugInfos) {
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

	private static Model loadModel(String filePathModel) {
		File modelFile = new File(filePathModel);

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
