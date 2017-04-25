package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import gui.MyMap;
import models.Constants;
import models.Edge;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class EntityFlowPainter implements IRoutePainter {

	private int timeStep = 0;
	private List<Edge> route;

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.IRoutePainter#setTimeStep(int)
	 */
	@Override
	public void setTimeStep(int time) {
		timeStep = time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#drawRoute(java.awt.Graphics2D, gui.MyMap)
	 */
	@Override
	public void drawRoute(Graphics2D g, MyMap map) {
		for (Edge edge : route) {

			int entityStepCurrent = timeStep / Constants.PAINT_STEPS;

			List<GeoPosition> points = edge.getPositions();

			int serviceTimeSteps = (edge.getServiceTime(entityStepCurrent) == 0 ? 1
					: edge.getServiceTime(entityStepCurrent));
			int pCount = points.size() / serviceTimeSteps;
			for (int i = 0; i < serviceTimeSteps; i++) {
				// Divide edge on the basis of the maxServiceTime
				int from = pCount * i;
				int to = from + pCount;
//				System.out.println("from: " + from + ", count: " + pCount + ", i: " + i);

				double stepFactor = timeStep % Constants.PAINT_STEPS / (double) Constants.PAINT_STEPS;

				int between = (int) (to * stepFactor);
				
				drawRoutePart(g, map, edge.getWorkload(entityStepCurrent - i), edge.getCapacity(entityStepCurrent - i),
						points, (int) (from * (1)), between);
				drawRoutePart(g, map, edge.getWorkload(entityStepCurrent - i - 1),
						edge.getCapacity(entityStepCurrent - i - 1), points, between, to);
			}

		}

	}

	public void drawRoutePart(Graphics2D g, MyMap map, long workload, long capacity, List<GeoPosition> points,
			int fromIndex, double toIndex) {
		if (capacity == 0) { // TODO Gradient
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(1.2f));
		} else {
			Color lineColor = DefaultRoutePainter.calculateColor(workload, capacity);
			g.setColor(lineColor);
			g.setStroke(new BasicStroke((float) (capacity / 75)));// TODO
																	// Gradient
		}
		GeoPosition last = null;
		// System.out.println(String.format("From: %d, To: %f", fromIndex,
		// toIndex));

		for (int i = fromIndex; i < toIndex && i < points.size(); i++) {
			GeoPosition point = points.get(i);
			if (last == null) {
				last = point;
				continue;
			}
			DefaultRoutePainter.drawNormalLine(g, map, last, point);
			last = point; // Important, else every edge consists of lines
							// from edge start to every step
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see painter.IRoutePainter#setRoute(java.util.List)
	 */
	@Override
	public void setRoute(List<Edge> route) {
		this.route = route;
	}

}
