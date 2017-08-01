package newVersion.models;

import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;

import models.Constants;
import models.Edge;
import newVersion.painter.NewEntityFlowPainter;

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
//		minMax[0] = 0;
//		minMax[1] =   edge.getPath().size();
		Path2D path = new Double();
//		for (Path2D path2d : fullPath) {
//		System.out.println(minMax[0] + " . " + minMax[1]);
		for(int i = minMax[0]; i < edge.getPath().size() && i <= minMax[1]; i++ ) {
			Path2D path2d = edge.getPath().get(i);
			path.append(path2d, Constants.drawLineWhenEnitityStarts);
		}
		capWorkIndex = minMax[1];
		return path;
	}
}
