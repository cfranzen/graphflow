/**
 * 
 */
package sea;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import models.Edge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaPointController {

	private List<SeaNode> points = new ArrayList<>();
	
	private int idCounter;
	
	
	public SeaNode createPointFromPos(GeoPosition pos) {
		return createPointFromPos(pos, new ArrayList<>());
	}
	
	public SeaNode createPointFromPos(GeoPosition pos, List<Long> edges) {
		SeaNode point = new SeaNode(idCounter++, pos, edges);
		points.add(point);
		return point;
	}
	
	public List<Edge> createEdges() {
		return null;
		
	}
	
	private class SeaNode {
		
		public long id;
		public GeoPosition pos;
		public List<Long> edges = new ArrayList<>();
		
		/**
		 * @param iD
		 * @param pos
		 */
		public SeaNode(long id, GeoPosition pos, List<Long> edges) {
			super();
			this.id = id;
			this.pos = pos;
			this.edges = edges;
		}
		
		public void addEdge(long nodeId) {
			edges.add(nodeId);
		}
	}
	
}
