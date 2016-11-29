/**
 * 
 */
package main;

import javax.swing.JFrame;

import gui.Map;
import models.ModelLoader;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Controller {

	public static void main(String[] args) {

		Controller controller = new Controller();
		controller.run();
	}

	
	private Map mapViewer;
	private ModelLoader input;
	private int currentTime = 0;
	
	
	/**
	 * 
	 */
	private void run() {
		mapViewer = new Map(this);

		// Display the viewer in a JFrame
		JFrame frame = new JFrame("JXMapviewer2 Example 2");
		frame.getContentPane().add(mapViewer);
		frame.setSize(1600, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		

		// Processing input to own classes
		input = ModelLoader.loadFile();

		// Add input to viewer
		mapViewer.addPositions(input.nodes);
		mapViewer.addEdges(input.edges);
		frame.setVisible(true);
	}

	/**
	 * 
	 */
	public void incTime() {
		currentTime++;
		if (currentTime > input.timesteps) {
			currentTime = 0;
		}
		mapViewer.setTime(currentTime);
		
	}

}
