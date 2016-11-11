package examples;

import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

/**
 * Displays a ring with one entity circling and coloring the ring accordingly to
 * the traffic.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RingWalker {

	public static final String ATTRIBUTE_COLOR = "ui.color";
	public static final int NODES = 25;

	private Graph graph;

	// @formatter:off
	// for testing purposes CSS file is better 
	protected static String styleSheet = 
		"edge {" 
		+ "	 size: 2px;" 
		+ "	 fill-color: green, yellow, red;"
		+ "	 fill-mode: dyn-plain;" 
		+ "}" 
		+ "node {" 
		+ "	 size: 12px;"
		+ "	 fill-color: #444;"
		+ "  text-mode: hidden;"
		+ "  text-alignment: under;" 
		+ "}"
		+ "node:clicked {" 
		+ "  fill-color: red;" 
		+ "}"
		+ "node:selected {" 
		+ "  fill-color: green;" 
		+ "  text-mode: normal;" 
		+ "  text-background-mode: plain;"
		+ "}";
	// @formatter:on

	/**
	 * For testing only
	 */
	public static void main(String[] args) {
		RingWalker.getRunningGraph().display();
	}

	/**
	 * Returns a {@link Graph} object with a moving entity
	 * 
	 * @return {@link Graph}
	 */
	public static Graph getRunningGraph() {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		RingWalker ring = new RingWalker();
		ring.Run();
		return ring.graph;
	}

	private RingWalker() {
		graph = new MultiGraph("Ring");
		addRingNodes(graph);
		// Rendering attributes
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
	}

	/**
	 * Starts a moving entity in an other thread
	 */
	public void Run() {
		RandomWalk walker = initWalker(graph);
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					walker.compute();
					UpdateGraph(graph, walker);
					try {
						Thread.sleep(80);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();
	}

	private void UpdateGraph(Graph g, RandomWalk rWalk) {
		for (Edge edge : g.getEachEdge()) {
			edge.setAttribute(ATTRIBUTE_COLOR, rWalk.getPasses(edge));
		}
	}

	private RandomWalk initWalker(Graph graph) {
		RandomWalk rWalker = new RandomWalk();
		rWalker.setEntityCount(1);
		rWalker.setEntityMemory(2);
		rWalker.setEvaporation(0.94);
		// to display only the current position
		// rWalker.setEvaporation(0.50);
		rWalker.init(graph);
		return rWalker;
	}

	private void addRingNodes(Graph g) {
		Node n = g.addNode("Node0");
		n.addAttribute("ui.label", n.getId());
		int i;
		for (i = 1; i < NODES; i++) {
			n = g.addNode("Node" + i);
			n.addAttribute("ui.label", n.getId());
			g.addEdge((i - 1) + "-" + i, "Node" + (i - 1), "Node" + i);
		}
		g.addEdge(i - 1 + "-0", "Node" + (i - 1), "Node0");
	}

}
