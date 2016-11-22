/**
 * 
 */
package main;

import javax.swing.JFrame;

import old.examples.ExampleModelLoading;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Main {

	public static void main(String[] args) {
		// new Gui(null).initGui();

		Map mapViewer = new Map();

		// Display the viewer in a JFrame
		JFrame frame = new JFrame("JXMapviewer2 Example 2");
		frame.getContentPane().add(mapViewer);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// Processing input to own classes
		ExampleModelLoading example = ExampleModelLoading.loadTestFile();

		mapViewer.addPositions(example.nodes);
		mapViewer.addEdges(example.edges);
		

	}

}
