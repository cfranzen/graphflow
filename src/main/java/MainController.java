import org.graphstream.graph.Graph;

import examples.RingWalker;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MainController {

	GuiBuilder gui;
	
	/**
	 * 
	 */
	public MainController() {
		gui = new GuiBuilder(this);
		gui.initGui();
		
	}
	
	public Graph getGraph() {
		return RingWalker.getRunningGraph();
	}
}
