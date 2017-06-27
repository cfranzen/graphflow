package newVersion.models;

import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;

import models.Edge;

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
	public Double getPath() {
		
		// TODO Pfad Abschnitt der Entity für den aktuellen time step als Path2D Objekt berechnen.
		
		return null ;
	}

}
