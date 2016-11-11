import org.graphstream.graph.Graph;

import examples.RingWalker;

/**
 * Main controller of the programm
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MainController {

	GuiBuilder gui;
	
	/**
	 * Constructor
	 */
	public MainController() {
		gui = new GuiBuilder(this);
		gui.initGui();
		
	}
	
	/**
	 * Returns a graph component from the model.
	 * @return
	 */
	public Graph getGraph() {
		return RingWalker.getRunningGraph();
	}
}
