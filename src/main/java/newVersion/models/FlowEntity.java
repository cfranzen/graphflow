package newVersion.models;

import models.Edge;
import newVersion.painter.NewEntityFlowPainter;

/**
 * This class should be created for every {@link Edge} on every time step. It
 * contains the serviceTime as TimeToLife. The painter adds the entities every
 * time step to the list which is to be drawn every frame.
 * <p>
 * Example found in {@link NewEntityFlowPainter}
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class FlowEntity {

	public int currentServiceTimeStep = 0;
	public int maxServiceTimeSteps = 0;
	public int capWorkIndex = 0;
	public NodeEdge edge;

	/**
	 * @param maxServiceTimeSteps
	 * @param capWorkIndex
	 */
	public FlowEntity(int maxServiceTimeSteps, int capWorkIndex, NodeEdge edge) {
		super();
		this.maxServiceTimeSteps = maxServiceTimeSteps;
		this.capWorkIndex = capWorkIndex;
		this.edge = edge;
	}

	public boolean next() {
		currentServiceTimeStep++;
		return currentServiceTimeStep <= maxServiceTimeSteps;
	}

}
