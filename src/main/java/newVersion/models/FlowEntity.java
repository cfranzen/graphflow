package newVersion.models;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class FlowEntity {

	public int currentServiceTimeStep = 0;
	public int maxServiceTimeSteps = 0;
	public int capWortIndex = 0;
	public NodeEdge edge;
	
	/**
	 * @param maxServiceTimeSteps
	 * @param capWortIndex
	 */
	public FlowEntity(int maxServiceTimeSteps, int capWortIndex, NodeEdge edge) {
		super();
		this.maxServiceTimeSteps = maxServiceTimeSteps;
		this.capWortIndex = capWortIndex;
		this.edge = edge;
	}

	public boolean next() {
		currentServiceTimeStep++;
		return currentServiceTimeStep <= maxServiceTimeSteps;
	}
	
}
