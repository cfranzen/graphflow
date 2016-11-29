package old.model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.swingViewer.LayerRenderer;
import org.graphstream.ui.view.Viewer;

import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;

import models.ModelLoader;

/**
 * 
 * Facade for the graphstream-framewotk
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class DynamicGraph extends MultiGraph {

	/**
	 * Configures the renderer to use antialiasing
	 */
	public static final String RENDER_ATTRIBUTE_ANTIALIAS = "ui.antialias";
	/**
	 * Configures the renderer to use high quality
	 */
	public static final String RENDER_ATTRIBUTE_QUALITY = "ui.quality";
	/**
	 * Defines the color of an {@link Edge} or a {@link Node}.
	 */
	public static final String ATTRIBUTE_COLOR = "ui.color";
	/**
	 * Defines the label of an {@link Edge} or a {@link Node}.
	 */
	public static final String ATTRIBUTE_LABEL = "ui.label";
	/**
	 * Defines the overall style of the graph.
	 */
	public static final String ATTRIBUTE_STYLESHEET = "ui.stylesheet";
	
	// @formatter:off
	protected static String styleSheet =
	"graph {"
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

	/**
	 * 
	 */
	public DynamicGraph() {
		super("defaultGraphId");
		
		// Rendering attributes
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		addAttribute(RENDER_ATTRIBUTE_QUALITY);
		addAttribute(RENDER_ATTRIBUTE_ANTIALIAS);
		addAttribute(ATTRIBUTE_STYLESHEET, styleSheet);

		// Processing input to own classes
		ModelLoader example = ModelLoader.loadFile();
//		for (Node node : example.nodes) {
//			org.graphstream.graph.Node newNode = addNode("" + node.getId());
//			newNode.setAttribute("xy", node.getLongitude(), node.getLatitude());
//			newNode.setAttribute("ui.label", node.getName());
//		}
//		for (Service edge : example.edges) {
//			addEdge("" + edge.getId(), "" + edge.getStartNode().getId(), "" + edge.getEndNode().getId());
//		}

		
	}

	public DefaultView getView() {
		Viewer viewer = new Viewer(this, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		GraphRenderer renderer = Viewer.newGraphRenderer();
		viewer.addView(Viewer.DEFAULT_VIEW_ID, renderer, false);
		viewer.disableAutoLayout();
		DefaultView view = (DefaultView) viewer.getDefaultView();
		return view;
	}
}
