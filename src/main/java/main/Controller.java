package main;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.shapes.GHPoint;

import gui.Map;
import gui.RunButton;
import models.ModelLoader;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Controller {

	public static void main(String[] args) {
		Controller controller = getInstance();
		controller.run();
	}

	private static Controller controller;

	private Map mapViewer;
	private ModelLoader input;
	private int currentTime = 0;

	private JFrame frame;

	public static Controller getInstance() {
		if (controller == null) {
			controller = new Controller();
		}
		return controller;
	}

	private Controller() {
		// for singleton

		// TEST

//		GraphHopper graphHopper = new GraphHopper().forDesktop();
//		GraphHopperStorage graph = graphHopper.getGraphHopperStorage();
//
//		graphHopper.importOrLoad();
//		
//		GHRequest ghRequest = new GHRequest(new GHPoint(50.11, 8.68), new GHPoint(50.11, 8.68)).setVehicle("car");
//		GHResponse response = graphHopper.route(ghRequest);
		
		// TEST

	}

	public void run() {

		// Display the viewer in a JFrame
		frame = new JFrame("Graphstream");
		frame.setSize(1600, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setBackground(Color.YELLOW);
		frame.getContentPane().setBackground(Color.RED);

		JLayeredPane layeredPane = new JLayeredPane();
		// layeredPane.setLayout(new BorderLayout() );

		layeredPane.setBackground(Color.BLUE);
		frame.getContentPane().add(layeredPane, BorderLayout.CENTER);

		System.out.println(frame.getSize().toString());
		// layeredPane.setSize(frame.getSize());
		System.out.println(layeredPane.getSize().toString());

		JButton btn = new RunButton(this);
		btn.setSize(150, 50);
		layeredPane.add(btn, new Integer(20));

		// Processing input to own classes
		input = ModelLoader.loadFile();

		// Add input to viewer
		mapViewer = new Map(this);
		mapViewer.addPositions(input.nodes);
		mapViewer.addEdges(input.edges);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(mapViewer, BorderLayout.CENTER);
		mapViewer.setSize(frame.getSize());
		p.setSize(frame.getSize());
		p.setBackground(Color.RED);

		layeredPane.add(p, new Integer(10));

		// TODO resize components on frame resize
		frame.setResizable(false);
		frame.setVisible(true);
	}

	/**
	 * 
	 */
	public void incTime() {
		currentTime++;
		if (currentTime >= input.timesteps) {
			currentTime = 0;
		}
		mapViewer.setTime(currentTime);
	}

	/**
	 * @return the mapViewer
	 */
	public Map getMapViewer() {
		return mapViewer;
	}
}
