/**
 * 
 */
package gui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class ImageWriter {

	/**
	 * Saves a screenshot of the given component to the working directory under
	 * "screenshots" with the current date.
	 * 
	 * @param comp
	 *            the {@link JComponent}
	 * @return the resulting save name.
	 */
	public static String makeScreenshot(JComponent comp) {
		BufferedImage bi = createImage(comp);
		return saveImage(bi);
	}

	private static String saveImage(BufferedImage bi) {
		final String directoryPath = "screenshots";
		final String filenamePath = "_Screenshot.png";
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String filename = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + filenamePath;
		try {
			ImageIO.write(bi, "png", new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return directoryPath + " /" + filename;
	}

	private static BufferedImage createImage(JComponent comp) {
		int w = comp.getWidth();
		int h = comp.getHeight();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		comp.paint(g);
		return bi;
	}

}
