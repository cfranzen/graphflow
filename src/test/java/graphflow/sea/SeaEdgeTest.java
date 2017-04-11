package graphflow.sea;

import org.junit.Test;
import org.jxmapviewer.viewer.GeoPosition;

import junit.framework.TestCase;
import models.SeaEdge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaEdgeTest extends TestCase {

	@Test
	public void testEquals() {
		
		GeoPosition start = new GeoPosition(0, 0);
		GeoPosition dest = new GeoPosition(10, 10);
		
		SeaEdge edge1 = new SeaEdge(start, dest);
		SeaEdge edge2 = new SeaEdge(start, dest);
		
		boolean condition = edge1.equals(edge2);
		assertTrue(condition);
	}
	
	@Test
	public void testEqualsReverse() {
		
		GeoPosition start = new GeoPosition(0, 0);
		GeoPosition dest = new GeoPosition(10, 10);
		
		SeaEdge edge1 = new SeaEdge(start, dest);
		SeaEdge edge2 = new SeaEdge(dest, start);
		
		boolean condition = edge1.equals(edge2);
		assertTrue(condition);
	}
	
}
