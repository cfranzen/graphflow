package models.nodeBased;

import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;

import main.Constants;
import models.pointBased.Edge;
import painter.NewEntityFlowPainter;

/**
 * Subclass of the {@link FlowEntity}-class, specialized for sea {@link Edge}s
 * where the points are not regular spread.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaFlowEntity extends FlowEntity {

	/**
	 * @param maxServiceTimeSteps
	 * @param capWorkIndex
	 * @param edge
	 */
	public SeaFlowEntity(int maxServiceTimeSteps, int capWorkIndex, NodeEdge edge) {
		super(maxServiceTimeSteps, capWorkIndex, edge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see newVersion.models.FlowEntity#getPath()
	 */
	@Override
	public Path2D getPath() {

		int[] minMax = NewEntityFlowPainter.calcMinMaxIndex(this);
		Path2D path = new Double();
		minMax[0] = minMax[0] > 0 ? minMax[0] : 0;
		minMax[1] = minMax[1] > 0 ? minMax[1] : 1;
		for (int i = minMax[0]; i < edge.getPath().size() && i <= minMax[1]; i++) {
			Path2D path2d = edge.getPath().get(i);
			path.append(path2d, Constants.drawLineWhenEnitityStarts);
		}
		int edgeElements = edge.getPath().size() * edge.nodes.size();
		capWorkIndex = minMax[1] / (edgeElements > 0 ? edgeElements : 1);
		return path;
	}
}
