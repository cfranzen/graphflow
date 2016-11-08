package examples;

import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

/**
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RandomTraffic {

	protected static String styleSheet = "edge {" + "	size: 2px;" + "	fill-color: green, yellow, red;"
			+ "	fill-mode: dyn-plain;" + "}" + "node {" + "	size: 6px;" + "	fill-color: #444;" + "}";

	/**
	 * Start program with
	 * <ul>
	 * <li>"java -Dsun.java2d.opengl=True" on linux</li>
	 * <li>"java -Dsun.java2d.directx=True" on windows</li>
	 * </ul>
	 * for better performance.
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		changeRenderer();

		Graph graph = createRandomGraph(100);
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.display();

		System.out.println(graph.getNodeCount());
		System.out.println(graph.getEdgeCount());

		RandomWalk rWalk;
		rWalk = new RandomWalk();
		rWalk.setEntityCount(graph.getNodeCount());
		rWalk.setEvaporation(0.99);
		rWalk.setEntityMemory(graph.getNodeCount() / 10);
		rWalk.init(graph);
		for (int i = 0; i < 30000; i++) {
			rWalk.compute();
			if (i % 10 == 0) {
				System.out.println("Step: " + i);
				UpdateGraph(graph, rWalk);
			}
			Thread.sleep(10);
		}
		rWalk.terminate();
		System.out.println("done");
		UpdateGraph(graph, rWalk);

	}

	private static Graph createRandomGraph(int nodeCount) {
		Graph graph = new MultiGraph("random");
		RandomGenerator rgen = new RandomGenerator(2.5);

		rgen.addSink(graph);
		rgen.begin();
		for (int i = 0; i < nodeCount; i++) {
			rgen.nextEvents();
		}
		rgen.end();

		for (Node node : graph.getEachNode()) {
			if (node.getEdgeSet().isEmpty()) {
				graph.removeNode(node);
			}
		}
		return graph;

	}

	private static void UpdateGraph(Graph graph, RandomWalk rWalk) {

		for (Edge edge : graph.getEachEdge()) {
			double passes = rWalk.getPasses(edge);
			double color = passes / (graph.getEdgeCount());
			edge.setAttribute("ui.color", color);
		}
	}

	/**
	 * Changes the graph renderer from default to the advanced viewer, which
	 * contains an automatic layout algorithm that will try to place the nodes
	 * so as to make the graph readable by adding a force to every node which
	 * repels all other nodes and attract those who are connected.
	 */
	public static void changeRenderer() {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		// org.graphstream.ui.swingViewer.GraphRenderer to use own renderer

	}

}
