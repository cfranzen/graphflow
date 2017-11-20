/**
 * 
 */
package interactiveWaypoints;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.JButton;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import models.CapacityWaypoint;

/**
 * "Paints" the Swing waypoints. In fact, just takes care of correct positioning
 * of the representing button.
 *
 */
public class SwingWaypointOverlayPainter extends WaypointPainter<CapacityWaypoint> {

	@Override
	protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int width, int height) {
		for (Waypoint waypoint : getWaypoints()) {
			Point2D point = jxMapViewer.getTileFactory().geoToPixel(waypoint.getPosition(), jxMapViewer.getZoom());
			Rectangle rectangle = jxMapViewer.getViewportBounds();
			int buttonX = (int) (point.getX() - rectangle.getX());
			int buttonY = (int) (point.getY() - rectangle.getY());
			JButton button = ((CapacityWaypoint) waypoint).getButton();
			button.setLocation(buttonX - button.getWidth() / 2, buttonY - button.getHeight() / 2);
			
			super.doPaint(g, jxMapViewer, width, height);
		}
	}
}
