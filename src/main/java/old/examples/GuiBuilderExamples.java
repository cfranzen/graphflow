package old.examples;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;

import org.graphstream.graph.Graph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.view.Viewer;

/**
 * Main class for gui controlling
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class GuiBuilderExamples {

	/**
	 * Creates and displays a panel which contains the given graph.
	 * 
	 * @param graph
	 *            which should be displayed.
	 */
	public static void createViewPanelOld(Graph graph) {

		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		GraphRenderer renderer = Viewer.newGraphRenderer();
		viewer.addView(Viewer.DEFAULT_VIEW_ID, renderer, false);
		Layout graphLayout = Layouts.newLayoutAlgorithm();
		viewer.enableAutoLayout(graphLayout);

		JFrame frame = new JFrame("mainFrame");
		frame.add((Component) viewer.getDefaultView());
		frame.setTitle(graph.getId());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		frame.add(viewer.getDefaultView());

		// contentPane.add(graph.getId(), viewer.getDefaultView());

		// frame.pack();
		frame.setSize(1600, 800);
		frame.setVisible(true);
	}

}
