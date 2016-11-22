/**
 * 
 */
package main;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.apache.commons.lang3.NotImplementedException;
import org.graphstream.ui.view.View;
import org.jxmapviewer.JXMapViewer;

import old.examples.GraphstreamGraph;
import old.examples.LeHavre;
import old.examples.RingWalker;
import old.main.GuiBuilder;
import old.main.MainController;
import old.model.MapGraphPanel;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Gui {

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
	public Gui(MainController controller) {
		this.controller = controller;
	}

	/**
	 * Initializes the user interface in its default size and registers the
	 * action listeners.
	 */
	public void initGui() {
		// mainFrame
		Dimension screen_dim = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame = new JFrame();
		mainFrame.setBounds(screen_dim.width / 20, screen_dim.height / 20, ((int) (screen_dim.width / 1.2)),
				((int) (screen_dim.height / 1.2)));
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
		lastAddedButton.setEnabled(false);
		lastAddedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(mainFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					controller.readGraph(fc.getSelectedFile());
//					displayGraph(controller.getGraph());
				} else {
					System.out.println("Open command cancelled by user." + "\n");
				}
			}

		});

		lastAddedButton = addButtonToButtonBar("Load example");
		lastAddedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SpringLayout layout = (SpringLayout) contentPane.getLayout();
//				Map map = new Map();
				
				JXMapViewer map = new JXMapViewer();
				layout.putConstraint(SpringLayout.WEST, map, 10, SpringLayout.EAST, pnlControl);
				layout.putConstraint(SpringLayout.EAST, map, -5, SpringLayout.EAST, contentPane);
				layout.putConstraint(SpringLayout.NORTH, map, 5, SpringLayout.NORTH, contentPane);
				layout.putConstraint(SpringLayout.SOUTH, map, -5, SpringLayout.SOUTH, contentPane);
				contentPane.add(map);

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
