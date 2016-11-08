import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

/**
 * 
 */

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RingWalker {

	public static final String ATTRIBUTE_COLOR = "ui.color";
	public static final int NODES = 25;

    protected static String styleSheet =
		"edge {"+
		"	size: 2px;"+
		"	fill-color: green, yellow, red;"+
		"	fill-mode: dyn-plain;"+
		"}"+
		"node {"+
		"	size: 6px;"+
		"	fill-color: #444;"+
		"}";
	
	public static void main(String[] args) throws InterruptedException {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		RingWalker ring = new RingWalker();
		ring.Run();
	}

	public void Run() throws InterruptedException {
		Graph graph = new MultiGraph("Ring");
		addRingNodes(graph);
		graph.addAttribute("ui.stylesheet", styleSheet);
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
		graph.display();

		RandomWalk walker = initWalker(graph);

		while(true){
			walker.compute();
			UpdateGraph(graph, walker);
			Thread.sleep(80);

		}
	}

	private void UpdateGraph(Graph g, RandomWalk rWalk) {
		for (Edge edge : g.getEachEdge()) {
			edge.setAttribute(ATTRIBUTE_COLOR, rWalk.getPasses(edge));
		}
	}

	private RandomWalk initWalker(Graph graph) {
		RandomWalk rWalker = new RandomWalk();
		rWalker.setEntityCount(1);
		rWalker.setEntityMemory(5);
		rWalker.setEvaporation(0.94);
		rWalker.init(graph);
		return rWalker;
	}

	private void addRingNodes(Graph g) {
		g.addNode("Node0");
		int i;
		for (i = 1; i < NODES; i++) {
			g.addNode("Node" + i);
			g.addEdge((i - 1) + "-" + i, "Node" + (i - 1), "Node" + i);
		}
		g.addEdge(i - 1 + "-0", "Node" + (i - 1), "Node0");
	}

}
