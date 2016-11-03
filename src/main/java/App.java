
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.Viewer;

/**
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class App {

	/**
	 * Start program with
	 * <ul>
	 * <li>"java -Dsun.java2d.opengl=True" on linux</li>
	 * <li>"java -Dsun.java2d.directx=True" on windows</li>
	 * </ul>
	 * for better performance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// simpleGraph();

		changeRenderer();

		multiGraph();

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

	private static void simpleGraph() {
		Graph graph = new SingleGraph("simpleGraph");
		graph.display();

		graph.addNode("A");
		graph.addNode("B");
		graph.addNode("C");
		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CA", "C", "A");

	}

	private static void multiGraph() {
		Graph graph = new MultiGraph("multiGraph");

		// GraphicGraph g = new GraphicGraph("gGraph");
		// String name = "ID";
		// HashMap<String, Object> attributes = new HashMap<String, Object>();
		// attributes.put("xyz", new int[] { 1, 23, 3 });
		// Node n = new GraphicNode(g, name, attributes);

		Node n = graph.addNode("A");
		n.setAttribute("xyz", 1, 3, 0);
		n = graph.addNode("B");
		n.setAttribute("xyz", 1, 2.5, 0);
		n = graph.addNode("C");
		n.setAttribute("xyz", 1, 5, 0);
		graph.addNode("D");
		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CA", "C", "A");
		graph.addEdge("DA", "D", "A");

		Viewer viewer = graph.display();

		// Only shows nodes which have coordinates node.setAttribute("xyz", 1,
		// 3, 0);
		viewer.disableAutoLayout(); // = graph.display(false); No auto-layout.
	}

}
