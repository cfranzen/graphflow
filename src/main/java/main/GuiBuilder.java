package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import examples.LeHavre;
import examples.RingWalker;
import model.GraphstreamGraph;
import model.MapGraphPanel;
import model.MapViewer;

/**
 * Gui controller
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class GuiBuilder {

	private static final int WIDTH_CONTROLS = 150;

	private MainController controller;

	private JFrame mainFrame;
	private JPanel pnlControl;
	private View view = null;
	private SpringLayout controlLayout = new SpringLayout();
	private JButton lastAddedButton;

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

	private void displayGraph(GraphstreamGraph graph) {
		Container contentPane = mainFrame.getContentPane();
		if (view != null) {
			// view.close((GraphicGraph) graph); // TODO change to graphic graph
			mainFrame.remove((Component) view);
			view = null;
		}

		Viewer viewer = new Viewer(graph.getGraphComponent(), Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		// Viewer viewer = graph.display(false);
		GraphRenderer renderer = Viewer.newGraphRenderer();
		viewer.addView(Viewer.DEFAULT_VIEW_ID, renderer, false);
		if (!controller.isAutoLayout()) {
			System.out.println("no auto layout");
			viewer.disableAutoLayout();
		} else {
			Layout graphLayout = Layouts.newLayoutAlgorithm();
			viewer.enableAutoLayout(graphLayout);
		}
		view = viewer.getDefaultView();

		Component viewComp = (Component) view;
		SpringLayout layout = (SpringLayout) contentPane.getLayout();
		layout.putConstraint(SpringLayout.WEST, viewComp, 10, SpringLayout.EAST, pnlControl);
		layout.putConstraint(SpringLayout.EAST, viewComp, -5, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, viewComp, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, viewComp, -contentPane.getHeight()/2, SpringLayout.SOUTH, contentPane);
		contentPane.add(viewComp);

		// TODO move/refactor
		Object[] d =  graph.getGraphComponent().getNode(0).getAttribute("xy");
		double longitude = (double) d[0];
		double latitude = (double) d[1];
		
		MapViewer mapViewer = new MapViewer();
		mapViewer.moveTo(longitude, latitude);
		
		// Delete me
		layout.putConstraint(SpringLayout.WEST, mapViewer, 10, SpringLayout.EAST, pnlControl);
		layout.putConstraint(SpringLayout.EAST, mapViewer, -5, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, mapViewer, 5, SpringLayout.SOUTH, viewComp);
		layout.putConstraint(SpringLayout.SOUTH, mapViewer, -5, SpringLayout.SOUTH, contentPane);
		contentPane.add(mapViewer);
		//--
		DefaultView defaultView = (DefaultView)(view);
		defaultView.setBackLayerRenderer(mapViewer);

		viewComp.addMouseWheelListener(mapViewer.getMouseWheelListeners()[0]);
		viewComp.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (view != null) {
					Camera camera = view.getCamera();
					int notches = e.getWheelRotation();
					if (notches < 0) {
						camera.setViewPercent(Math.max(0.0001f, camera.getViewPercent() * 0.9f));
					} else if (notches > 0) {
						camera.setViewPercent(camera.getViewPercent() * 1.1f);
					}
				}
			}
		});
		
		
		
		mainFrame.revalidate();
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
		pnlControl.setLayout(controlLayout);
		layout.putConstraint(SpringLayout.WEST, pnlControl, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, pnlControl, WIDTH_CONTROLS, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, pnlControl, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, pnlControl, -5, SpringLayout.SOUTH, contentPane);
		contentPane.add(pnlControl);

		lastAddedButton = addButtonToButtonBar("Load graph");
		lastAddedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(mainFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					controller.readGraph(fc.getSelectedFile());
					displayGraph(controller.getGraph());
				} else {
					System.out.println("Open command cancelled by user." + "\n");
				}
			}
		});

		lastAddedButton = addButtonToButtonBar("Load ring graph");
		lastAddedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.loadGraph(RingWalker.class.getName());
				displayGraph(controller.getGraph());

			}
		});

		lastAddedButton = addButtonToButtonBar("Load le havre");
		lastAddedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.loadGraph(LeHavre.class.getName());
				displayGraph(controller.getGraph());

			}
		});

		lastAddedButton = addButtonToButtonBar("Load example");
		lastAddedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// controller.loadGraph(LeHavre.class.getName());
				controller.loadExample();
//				displayGraph(controller.getGraph());
				
				SpringLayout layout = (SpringLayout) contentPane.getLayout();
				MapGraphPanel viewComp = new MapGraphPanel();
				layout.putConstraint(SpringLayout.WEST, viewComp, 10, SpringLayout.EAST, pnlControl);
				layout.putConstraint(SpringLayout.EAST, viewComp, -5, SpringLayout.EAST, contentPane);
				layout.putConstraint(SpringLayout.NORTH, viewComp, 5, SpringLayout.NORTH, contentPane);
				layout.putConstraint(SpringLayout.SOUTH, viewComp, -contentPane.getHeight()/2, SpringLayout.SOUTH, contentPane);
				contentPane.add(viewComp);
				
				layout.putConstraint(SpringLayout.WEST, viewComp.mapViewer, 10, SpringLayout.EAST, pnlControl);
				layout.putConstraint(SpringLayout.EAST, viewComp.mapViewer, -50, SpringLayout.EAST, contentPane);
				layout.putConstraint(SpringLayout.NORTH, viewComp.mapViewer, 5, SpringLayout.SOUTH, viewComp);
				layout.putConstraint(SpringLayout.SOUTH, viewComp.mapViewer, -5, SpringLayout.SOUTH, contentPane);
				contentPane.add(viewComp.mapViewer);
				
				mainFrame.revalidate();
			}
		});

		mainFrame.setVisible(true);
	}

	/**
	 * @param string
	 */
	private JButton addButtonToButtonBar(String caption) {
		JButton newBtn = new JButton(caption);
		controlLayout.putConstraint(SpringLayout.WEST, newBtn, 0, SpringLayout.WEST, pnlControl);
		controlLayout.putConstraint(SpringLayout.EAST, newBtn, 0, SpringLayout.EAST, pnlControl);
		if (lastAddedButton != null) {
			controlLayout.putConstraint(SpringLayout.NORTH, newBtn, 10, SpringLayout.SOUTH, lastAddedButton);
		}
		pnlControl.add(newBtn);
		return newBtn;
	}

}
