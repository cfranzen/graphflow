package models.nodeBased;

import java.awt.geom.Path2D;
import java.util.List;

import models.pointBased.Edge;
import painter.NewEntityFlowPainter;

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

	public int startTimestep = 0;
	public int capWorkIndex = 0;
	private int currentServiceTimeStep = 0;
	private int maxServiceTimeSteps = 0;
	public NodeEdge edge;

	/**
	 * Creates a new object of the {@link FlowEntity}-Class, which represents a
	 * moving entity on the map.
	 * 
	 * @param maxServiceTimeSteps
	 *            the time this entity needs to reach its destination.
	 * @param capWorkIndex
	 *            the time step when the entity starts moving to identify his
	 *            impact on the edges.
	 */
	public FlowEntity(int maxServiceTimeSteps, int capWorkIndex, NodeEdge edge) {
		super();
		this.maxServiceTimeSteps = maxServiceTimeSteps;
		this.startTimestep = capWorkIndex;
		this.edge = edge;
	}

	/**
	 * Increases the time step counter and returns <code>true</code> if the
	 * entity is under his maximum and so alive or <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if the entity is still alive<br>
	 *         <code>false</code> if the entity reached his maximum time to live
	 */
	public boolean next() {
		currentServiceTimeStep++;
		return currentServiceTimeStep <= maxServiceTimeSteps;
	}

	/**
	 * Returns the {@link List} of {@link MapNode} which the {@link FlowEntity}
	 * passes on its way.
	 * 
	 * @return {@link List} of {@link MapNode}s
	 */
	public List<MapNode> getPoints() {
		return edge.getPoints();
	}

	/**
	 * Returns the maximal time the {@link FlowEntity} needs to reach its
	 * destination.
	 * 
	 * @return maximal time steps as integer
	 */
	public int getMaxServiceTimeSteps() {
		return maxServiceTimeSteps;
	}

	/**
	 * Returns the current time step of the entity which can be seen as time to
	 * life until this value reaches the number of the max service time step
	 * variable.
	 * 
	 * @return the time steps this entity already lives as integer
	 */
	public int getCurrentServiceTimeStep() {
		return currentServiceTimeStep;
	}

	/**
	 * If one of the subclasses contains already a {@link Path2D} then use this
	 * instead of simple line drawing. The {@link Path2D} should be altered for
	 * every time step.
	 * @param map 
	 * 
	 * @return
	 */
	public Path2D getPath() {
		return null;
	}

}
