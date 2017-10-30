/**
 * 
 */
package painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import interactiveWaypoints.SwingWaypoint;
import models.CapacityWaypoint;
import models.Constants;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class CapacityWaypointRenderer implements WaypointRenderer<Waypoint> {

	private static final Log log = LogFactory.getLog(DefaultWaypointRenderer.class);
	private BufferedImage img = null;

	/**
	 * Uses a simple circle instead of an image
	 */
	public CapacityWaypointRenderer() {
	}

	/**
	 * Uses an image as {@link Waypoint}-symbol
	 * 
	 * @param imageFilePath
	 */
	public CapacityWaypointRenderer(String imageFilePath) {
		try {
			img = ImageIO.read(DefaultWaypointRenderer.class.getResource(imageFilePath));
		} catch (Exception ex) {
			log.warn("couldn't read the file" + imageFilePath, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jxmapviewer.viewer.WaypointRenderer#paintWaypoint(java.awt.
	 * Graphics2D, org.jxmapviewer.JXMapViewer, java.lang.Object)
	 */
	@Override
	public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint) {
		Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());
		boolean debug = true;
		
		if (img == null) {
			drawCircleWaypoint(g, point);
		} else {
			drawImageWaypoint(g, point);
		}

	}

	private void drawCircleWaypoint(Graphics2D g, Point2D point) {
		int x = (int) point.getX() - (Constants.CIRCLE_DIAMETER / 2);
		int y = (int) point.getY() - (Constants.CIRCLE_DIAMETER / 2);

		g.setColor(getWaypointColor());
		g.fillOval(x, y, Constants.CIRCLE_DIAMETER, Constants.CIRCLE_DIAMETER);
	}

	private Color getWaypointColor() {
		// TODO Gradient
		return Color.BLACK;
	}

	private void drawImageWaypoint(Graphics2D g, Point2D point) {

		int x = (int) point.getX() - img.getWidth() / 2;
		int y = (int) point.getY() - img.getHeight();

		g.drawImage(img, x, y, null);
	}

}
