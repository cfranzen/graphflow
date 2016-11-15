package main;

import java.io.File;

import org.graphstream.graph.Graph;

import model.DynamicGraph;
import model.Graphs;

/**
 * Main controller of the programm
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MainController {

	private GuiBuilder gui;
	private Graphs model;
	private boolean isActive = false;

	/**
	 * Constructor
	 */
	public MainController() {
		model = null;
		gui = new GuiBuilder(this);
		gui.initGui();
	}

	public void readGraph(File file) {
		// TODO
	}

	public void loadGraph(String className) {
		if (isActive) {
			model.destroy();
		}
		isActive = true;

		try {
			Object graphClass = Class.forName(className).newInstance();
			if (graphClass instanceof Graphs) {
				model = (Graphs) graphClass;
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a graph component from the model.
	 * 
	 * @return
	 */
	public Graph getGraph() {
		return model.getRunningGraph();
	}

	
	public boolean isAutoLayout() {
		return model.isAutoLayout();
	}
	
	/**
	 * 
	 */
	public void loadExample() {
		model = new DynamicGraph();

	}
}
