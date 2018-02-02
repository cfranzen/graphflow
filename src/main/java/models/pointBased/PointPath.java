package models.pointBased;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Facade for the {@link Path2D} class for easier usage.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class PointPath {

	private Path2D path;
	private int pointCount = 0;

	/**
	 * Initializes the {@link Path2D} member variable.
	 */
	public PointPath() {
		path = new Path2D.Double();
	}

	public void moveTo(double x, double y) {
		path.moveTo(x, y);
		pointCount++;
	}

	public void moveTo(Point2D point) {
		moveTo(point.getX(), point.getY());
	}

	public void lineTo(double x, double y) {
		path.lineTo(x, y);
		pointCount++;
	}

	public void lineTo(Point2D point) {
		lineTo(point.getX(), point.getY());
	}

	public void quadTo(double x1, double y1, double x2, double y2) {
		path.quadTo(x1, y1, x2, y2);
		pointCount += 2;
	}

	public void quadTo(Point2D p1, Point2D p2) {
		path.quadTo(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		pointCount += 2;
	}

	public Path2D getPath() {
		return (Path2D) path.clone();
	}

	/**
	 * Returns the number of points for this path segment.
	 * 
	 * @return the pointCount
	 */
	public int getPointCount() {
		return pointCount;
	}

}
