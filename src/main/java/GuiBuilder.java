import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.graphstream.graph.Graph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.view.Viewer;

/**
 * Gui controller
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class GuiBuilder {

	private MainController controller;

	private JFrame mainFrame;
	private JPanel pnlControl;
	private JPanel pnlGraph = null;

	/**
	 * Constructor, creates a new {@link GuiBuilder}-object and saves a
	 * reference to the controller.
	 * 
	 * @param controller
	 *            reference to the {@link MainController}-object
	 */
	public GuiBuilder(MainController controller) {
		this.controller = controller;
	}

	private void loadGraph() {
		if (pnlGraph == null) {

			Graph graph = controller.getGraph();
			// Graph graph = RingWalker.getRunningGraph(); // TODO FileChooser

			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			GraphRenderer renderer = Viewer.newGraphRenderer();
			viewer.addView(Viewer.DEFAULT_VIEW_ID, renderer, false);
			Layout graphLayout = Layouts.newLayoutAlgorithm();
			viewer.enableAutoLayout(graphLayout);
			pnlGraph = viewer.getDefaultView();

			Container contentPane = mainFrame.getContentPane();

			SpringLayout layout = (SpringLayout) contentPane.getLayout();
			layout.putConstraint(SpringLayout.WEST, pnlGraph, 10, SpringLayout.EAST, pnlControl);
			layout.putConstraint(SpringLayout.EAST, pnlGraph, -5, SpringLayout.EAST, contentPane);
			layout.putConstraint(SpringLayout.NORTH, pnlGraph, 5, SpringLayout.NORTH, contentPane);
			layout.putConstraint(SpringLayout.SOUTH, pnlGraph, -5, SpringLayout.SOUTH, contentPane);
			contentPane.add(pnlGraph);

			mainFrame.revalidate();
		}
	}

	/**
	 * Initializes the user interface in its default size and registers the
	 * action listeners.
	 */
	public void initGui() {
		// mainFrame
		Dimension screen_dim = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame = new JFrame();
		mainFrame.setBounds(screen_dim.width / 5, screen_dim.height / 5, ((int) (screen_dim.width / 1.5)),
				((int) (screen_dim.height / 1.5)));
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = mainFrame.getContentPane();
		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);
		contentPane.setBackground(Color.GREEN);

		// button bar
		pnlControl = new JPanel();
		pnlControl.setBackground(Color.RED);
		SpringLayout controlLayout = new SpringLayout();
		pnlControl.setLayout(new SpringLayout());
		layout.putConstraint(SpringLayout.WEST, pnlControl, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, pnlControl, 105, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, pnlControl, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, pnlControl, -5, SpringLayout.SOUTH, contentPane);
		contentPane.add(pnlControl);

		JButton btnLoad = new JButton("Load graph");
		// TODO file chooser
		btnLoad.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadGraph();

			}
		});
		controlLayout.putConstraint(SpringLayout.WEST, btnLoad, 0, SpringLayout.WEST, pnlControl);
		controlLayout.putConstraint(SpringLayout.EAST, btnLoad, 0, SpringLayout.EAST, pnlControl);
		pnlControl.add(btnLoad);

		mainFrame.setVisible(true);
	}

}
