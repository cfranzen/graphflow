package model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public interface Graphs {

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

	/**
	 * Returns the {@link Graph} from the model.
	 * 
	 * @return {@link Graph}
	 */
	Graph getGraph();

	/**
	 * Returns an active running {@link Graph} in its own thread.
	 * 
	 * @return {@link Graph}
	 */
	Graph getRunningGraph();

	default void configureRenderer(Graph graph) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		// Rendering attributes
		graph.addAttribute(RENDER_ATTRIBUTE_QUALITY);
		graph.addAttribute(RENDER_ATTRIBUTE_ANTIALIAS);
	}

	/**
	 * Stops the simulation and releases the memory
	 */
	void destroy();

	/**
	 * Informs the renderer if it should render the graph with auto or static
	 * layout.
	 * 
	 * @return <code>true</code> if auto-layout should be used </br>
	 *         <code>false</code> if not
	 */
	boolean isAutoLayout();

}
