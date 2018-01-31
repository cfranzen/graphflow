/**
 * 
 */
package graphflow.sea;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import junit.framework.TestCase;
import main.MainController;
import newVersion.models.SeaNode;
import sea.SeaNodeFactory;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaControllerTest extends TestCase {

//	private SeaNodeFactory instance = SeaNodeFactory.getInstance(MainController.getInstance().getMapViewer());
	
	public void testCreateFromData() {
		SeaNode result = new SeaNode(1, new GeoPosition(new double[]{57.621875380195455, 7.14111328125}), new ArrayList<>());
		result.addEdge(0);
		result.addEdge(2);
		assertTrue(result.toString().contains("[id=1,pos=[57.621875380195455, 7.14111328125],edges=[0, 2]]"));
	}
	
	
	
	public void testParseLine() {
		String line = "sea.SeaPointController$SeaNode@4973813a[id=1,pos=[57.621875380195455, 7.14111328125],edges=[0, 2]]";
		SeaNode result = new SeaNode(line);
		assertTrue(result.toString().contains("[id=1,pos=[57.621875380195455, 7.14111328125],edges=[0, 2]]"));
	}
	
//	public void testReadFile() {
//		String filepath = "src/test/resources/graphflow/sea/nodes";
//		List<SeaNode> nodes = instance.addEdgesFromFile(filepath);
//		assertEquals(2, nodes.size());
//		assertTrue(nodes.get(0).toString().contains("[id=1,pos=[57.621875380195455, 7.14111328125],edges=[0, 2]]"));
//		assertTrue(nodes.get(1).toString().contains("[id=2,pos=[54.34214886448341, 7.66845703125],edges=[1, 3]]"));
//	}
}
