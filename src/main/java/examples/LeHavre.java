package examples;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import model.GraphstreamGraph;

/**
 * Test class for the graphstream framework. The class displays a city with
 * streets where the speedlimit and lanes are highlighted
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class LeHavre implements GraphstreamGraph {

	private static final double MAX_SPEED = 130.0;
	private static final double SIZE_FACTOR_EDGE = 2.5;
	private static final Path STYLESHEET_PATH = FileSystems.getDefault().getPath("src", "main", "resources", "examples",
			"LeHavreStyleSheet.css");

	private String stylesheet;
	private Graph graph;

	public static void main(String args[]) {

		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		LeHavre leHavre = new LeHavre();
		leHavre.display();
	}

	/**
	 * Creates the graph example "Le Havre" from an dgs input file.
	 */
	public LeHavre() {
		readStylesheet();
		graph = processInput();
		configureGraphRender(graph);
	}

	private void readStylesheet() {
		try {
			System.out.println(STYLESHEET_PATH.toAbsolutePath());
			StringBuffer buffer = new StringBuffer();

			for (String line : Files.readAllLines(STYLESHEET_PATH)) {
				buffer.append(line);
			}
			stylesheet = buffer.toString();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * Creates and displays a panel which contains the given graph.
	 * 
	 * @param graph
	 *            which should be displayed.
	 */
	private void display() {
		Viewer viewer = graph.display(false); // Creates the window
		View view = viewer.getDefaultView();
		((ViewPanel) view).resizeFrame(1600, 800);
		view.getCamera().setViewPercent(0.5);
	}

	/**
	 * Adds some rendering properties.
	 * 
	 * @param graph
	 *            which rendering should be changed.
	 */
	private void configureGraphRender(Graph graph) {
		graph.addAttribute("ui.quality"); //
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet", stylesheet);
	}

	/**
	 * Reads the input file and creates the graph.
	 * 
	 * @return created {@link Graph} from input file.
	 */
	private Graph processInput() {
		Graph graph = new MultiGraph("Le Havre");
		String inputFilePath = "src/main/resources/examples/LeHavre.dgs";
		try {
			graph.read(inputFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		addDesignClassesFromInput(graph);
		System.out.println("readFile - done");
		return graph;
	}

	/**
	 * Reads the attributes from the Input and adds the specific classes from
	 * the CSS.
	 * 
	 * @param graph
	 *            which contains edges with attributes.
	 */
	private void addDesignClassesFromInput(Graph graph) {
		for (Edge edge : graph.getEachEdge()) {
			if (edge.hasAttribute("isTollway")) {
				edge.addAttribute("ui.class", "tollway");
			} else if (edge.hasAttribute("isTunnel")) {
				edge.addAttribute("ui.class", "tunnel");
			} else if (edge.hasAttribute("isBridge")) {
				edge.addAttribute("ui.class", "bridge");
			}

			double speedMax = edge.getNumber("speedMax") / MAX_SPEED;
			edge.setAttribute("ui.color", speedMax);

			double lanes = edge.getNumber("lanes");
			if (lanes > 1) {
				lanes *= SIZE_FACTOR_EDGE;
			}
			edge.setAttribute("ui.size", lanes);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.Graphs#getGraph()
	 */
	@Override
	public Graph getGraphComponent() {
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.PredefinedGraphs#getRunningGraph()
	 */
	@Override
	public Graph getRunningGraph() {
		// no entities
		return getGraphComponent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.Graphs#destroy()
	 */
	@Override
	public void destroy() {
		graph.clear();
	}

	/* (non-Javadoc)
	 * @see model.Graphs#isAutoLayout()
	 */
	@Override
	public boolean isAutoLayout() {
		return false;
	}

}
