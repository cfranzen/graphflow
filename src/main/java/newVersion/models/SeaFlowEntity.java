package newVersion.models;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Path2D.Double;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import models.Edge;
import models.HighResEdge;
import models.PointPath;

/**
 * Subclass of the {@link FlowEntity}-class, specialized for sea {@link Edge}s
 * where the points are not regular spread.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class SeaFlowEntity extends FlowEntity {

	private List<Path2D> fullPath;
	
	/**
	 * @param maxServiceTimeSteps
	 * @param capWorkIndex
	 * @param edge
	 */
	public SeaFlowEntity(int maxServiceTimeSteps, int capWorkIndex, NodeEdge edge) {
		super(maxServiceTimeSteps, capWorkIndex, edge);
	}

	public void setPath(List<Path2D> fullPath) {
		this.fullPath = fullPath;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see newVersion.models.FlowEntity#getPath()
	 */
	@Override
	public Double getPath() {
		
	return null;
	}
//		// TODO Pfad Abschnitt der Entity für den aktuellen time step als Path2D Objekt berechnen.
//		GeoPosition lastPos = null;
//		Point2D lastCircleP = null;
//		for (int j = 1; j < edge.getPositions().size(); j++) {
//			GeoPosition pos = edge.getPosition(j);
//
//			Point2D circlePIn = getCirclePoint(pos, lastPos);
//			Point2D circlePOut = getCirclePoint(lastPos, pos);
//			PointPath path = new PointPath();
//		
//			path.moveTo(lastCircleP);
//			path.quadTo(convertGeo(map, lastPos), circlePIn);
////			addDrawEdge(edge, convertPoint(map, lastCircleP), convertPoint(map, circlePIn), path);
////
////			path = new PointPath();
////			path.moveTo(circlePIn);
////			path.lineTo(circlePOut);
////			addDrawEdge(edge, convertPoint(map, circlePIn), convertPoint(map, circlePOut), path);
//
//			lastPos = pos;
//			lastCircleP = circlePOut;
//		
//		}	
//		
//		
//		
//		
//		return null ;
//	}
//
//	/**
//	 * see {@link #getCirclePoint(Point2D, Point2D)}
//	 * 
//	 * @param linePoint
//	 * @param circlePoint
//	 * @return crossing {@link Point2D} of line and circle.
//	 */
//	private Point2D getCirclePoint(GeoPosition linePoint, GeoPosition circlePoint) {
//		return getCirclePoint(convertGeo(linePoint), convertGeo(circlePoint));
//	}
//
//	/**
//	 * Converts an {@link GeoPosition} to a {@link Point2D}
//	 * 
//	 * @param geopos
//	 *            {@link GeoPosition} to convert
//	 * @return the corresponding {@link Point2D} to the {@link GeoPosition}
//	 */
//	private Point2D convertGeo(MyMap map, GeoPosition geopos) {
//		return map.getTileFactory().geoToPixel(geopos, map.getZoom());
//	}
//
//	/**
//	 * Converts the given {@link Point2D} to the corresponding
//	 * {@link GeoPosition}
//	 * 
//	 * @param point
//	 *            {@link Point2D} to convert
//	 * @return the corresponding {@link GeoPosition} to the {@link Point2D}
//	 */
//	private GeoPosition convertPoint(MyMap map, Point2D point) {
//		return map.getTileFactory().pixelToGeo(point, map.getZoom());
//	}
}
