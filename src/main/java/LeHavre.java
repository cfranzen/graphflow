
import java.awt.Component;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFrame;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

/**
 * Test class for the graphstream framework
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class LeHavre {

	private static final double MAX_SPEED = 130.0;
	private static final Path STYLESHEET_PATH = FileSystems.getDefault().getPath("src", "main", "resources",
			"LeHavreStyleSheet.css");
	private static String stylesheet;

	public static void main(String args[]) {
		try {
			System.out.println(STYLESHEET_PATH.toAbsolutePath());
			StringBuffer buffer = new StringBuffer();

			for (String line : Files.readAllLines(STYLESHEET_PATH)) {
				buffer.append(line);
			}
			stylesheet = buffer.toString();
			System.out.println(stylesheet);

			App.changeRenderer();

			new LeHavre();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public LeHavre() {
		Graph graph = processInput();
		configureGraphRender(graph);
		// createViewPanel(graph);

		// Viewer viewer = new Viewer(graph,
		// Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

		View view = viewer.addDefaultView(false);

		JFrame frame = new JFrame("title");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add((Component) view);

		frame.setSize(1600, 800);

		frame.setVisible(true);

	}

	private void createViewPanel(Graph graph) {
		// graph.display(false); // false = No auto-layout.

		Viewer viewer = graph.display(false); // Creates the window
		View view = viewer.getDefaultView();
		((ViewPanel) view).resizeFrame(1600, 800);
		view.getCamera().setViewCenter(440000, 2503000, 0);
		view.getCamera().setViewPercent(0.25);
	}

	private void configureGraphRender(Graph graph) {
		graph.addAttribute("ui.quality"); //
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet", stylesheet);
	}

	private Graph processInput() {
		Graph graph = new MultiGraph("Le Havre");
		String inputFilePath = "src/main/resources/LeHavre.dgs";
		try {
			graph.read(inputFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		addDesignClassesFromInput(graph);
		return graph;
	}

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
		}
	}
}
