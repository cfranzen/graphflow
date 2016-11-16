package model;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.GraphicGraph;

import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;

import examples.ExampleModelLoading;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class DynamicGraph implements GraphstreamGraph {

	// @formatter:off
	protected static String styleSheet =
	"graph {"
	+ "fill-color: rgba(0, 0, 0, 0);"
	+ "}"
	+ "edge {" 
	+ "	size: 2px;" 
	+ "	fill-color: green, yellow, red;"
	+ "	fill-mode: dyn-plain;" 
	+ "}"
	+ "node {" 
	+ "	 size: 6px;"
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

	private Graph graph;

	/**
	 * Constructor
	 */
	public DynamicGraph() {
		// readStylesheet();
		graph = processInput();
		configureRenderer(graph);
		graph.addAttribute(ATTRIBUTE_STYLESHEET, styleSheet);
	}

	/**
	 * 
	 */
	private Graph processInput() {
		Graph graph = new MultiGraph("Example");
		

		ExampleModelLoading example = ExampleModelLoading.loadTestFile();
		for (Node node : example.nodes) {
			org.graphstream.graph.Node newNode = graph.addNode("" + node.getId());
			newNode.setAttribute("xy", node.getLongitude(), node.getLatitude());
			newNode.setAttribute("ui.label", node.getName());
		}
		for (Service edge : example.edges) {
			graph.addEdge("" + edge.getId(), "" + edge.getStartNode().getId(), "" + edge.getEndNode().getId());
		}

		return graph;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.Graphs#getRunningGraph()
	 */
	@Override
	public Graph getRunningGraph() {
		// TODO Auto-generated method stub
		return getGraphComponent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.Graphs#getGraphComponent()
	 */
	@Override
	public Graph getGraphComponent() {
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.Graphs#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.Graphs#isAutoLayout()
	 */
	@Override
	public boolean isAutoLayout() {
		return false;
	}

}
