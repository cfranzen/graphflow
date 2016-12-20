package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.util.GeoUtil;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import gui.MyMap;
import gui.RunButton;
import models.Edge;
import models.HighResEdge;
import models.ModelLoader;

/**
 * Main class from the program
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Controller {

	public static void main(String[] args) {
		Controller controller = getInstance();
		controller.run();
	}

	private static Controller controller;

	private ForkJoinPool pool;

	private MyMap mapViewer;
	private ModelLoader input;
	private GraphHopper graphHopper;
	private int currentTime = 0;

	private JFrame frame;

	/**
	 * Returns the {@link Controller} instance, if the instance is
	 * <code>null</code> a new instance is created.
	 * 
	 * @return the {@link Controller} instance
	 */
	public static Controller getInstance() {
		if (controller == null) {
			controller = new Controller();
		}
		return controller;
	}

	/**
	 * Private constructor to override the default and ensure the singleton
	 * pattern.
	 */
	private Controller() {
		pool = new ForkJoinPool();
	}

	/**
	 * Main method.
	 * 
	 * TODO distinct gui creation
	 */
	public void run() {

		// Display the viewer in a JFrame
		frame = new JFrame("Graphstream");
		frame.setSize(1600, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setBackground(Color.YELLOW);
		frame.getContentPane().setBackground(Color.RED);

		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setBackground(Color.BLUE);
		frame.getContentPane().add(layeredPane, BorderLayout.CENTER);

		System.out.println(frame.getSize().toString());
		// layeredPane.setSize(frame.getSize());
		System.out.println(layeredPane.getSize().toString());

		JButton btn = new RunButton(this);
		btn.setSize(150, 50);
		layeredPane.add(btn, new Integer(20));

		// Processing input to own classes
		input = ModelLoader.loadFile();

		// Add input to viewer
		mapViewer = new MyMap(this);
		mapViewer.addPositions(input.nodes);
		mapViewer.addEdges(input.edges);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(mapViewer, BorderLayout.CENTER);
		mapViewer.setSize(frame.getSize());
		p.setSize(frame.getSize());
		p.setBackground(Color.RED);

		layeredPane.add(p, new Integer(10));

		// TODO resize components on frame resize
		frame.setResizable(true);
		frame.setVisible(true);

		// init GH
		long time = System.currentTimeMillis();
		System.out.println("Start GH");
		initGraphhopper();
		// optimize GH edges
		mapEdgesToStreets();
		System.out.println("End GH - " + (System.currentTimeMillis() - time));

		optimizeEdges();
		optimizeEdges();
	}

	/**
	 * Increases the current time step each time its called. If the maximum time
	 * step from the model is reached it resets the step to zero.
	 */
	public void incTime() {
		currentTime++;
		if (currentTime >= input.timesteps) {
			currentTime = 0;
		}
		mapViewer.setTime(currentTime);
	}

	/**
	 * Returns the map component
	 * 
	 * @return the mapViewer
	 */
	public MyMap getMapViewer() {
		return mapViewer;
	}

	private void optimizeEdges2() {
		calcAllPoints();

		List<Edge> edges = mapViewer.getRoute();
		for (Edge edge : mapViewer.getRoute()) {
			if (edge.getPoints().size() == 1) {
				edges.remove(edge);
			}
		}

		outer: for (int i = 0; i < edges.size(); i++) {
			Edge edge = edges.get(i);
			List<Double[]> points = edge.getPoints();

			for (int j = 0; j < points.size(); j++) {
				Double[] point = points.get(j);

				for (int k = 0; k < edges.size(); k++) {
					Edge edge2 = edges.get(k);
					List<Double[]> points2 = edge2.getPoints();

					for (int l = 0; l < points2.size(); l++) {
						Double[] point2 = points2.get(l);

						if (isNear(point, point2)) {
							// Begin common Part
							boolean sameDirection = false;
							if (points.size() <= j + 1 || points2.size() <= l + 1) {
								continue outer; // TODO refactor
							}
							if (isNear(points.get(j + 1), points2.get(l + 1))) {
								sameDirection = true;
							}
							List<Double[]> commonPart = new ArrayList<>();
							// cut edges and save only old part
							// Check direction
							List<Double[]> old1 = points.subList(0, j);
							List<Double[]> new1 = points.subList(j, points.size());

							edge = new HighResEdge(edge);
							((HighResEdge) edge).addPositions(old1);

							List<Double[]> old2;
							List<Double[]> new2;
							if (sameDirection) {
								old2 = points2.subList(0, l);
								new2 = points2.subList(l, points2.size());
							} else {
								new2 = points2.subList(0, l);
								old2 = points2.subList(l, points2.size());
							}
							edge2 = new HighResEdge(edge2);
							((HighResEdge) edge2).addPositions(old2);

							// while 1 near 2 add to common
							int m;
							for (m = 0; m < new1.size() && m < new2.size(); m++) {
								Double[] p1 = new1.get(m);
								Double[] p2 = new2.get(m);
								if (isNear(p1, p2)) {
									commonPart.add(p1);
								}
							}
							new1 = new1.subList(m, new1.size());
							new2 = new2.subList(m, new2.size());
							// after save rest as new edges -> will be optimzed
							// later
							edges.set(i, edge);
							edges.set(k, edge2);

							HighResEdge commonEdge = new HighResEdge();
							commonEdge.addPositions(commonPart);
								edges.add(commonEdge);
						}

					}

				}

			}
		}
		System.out.println((edges.equals(mapViewer.getRoute())));
		mapViewer.setRoute(edges);
		calcAllPoints();
		
		for (Edge edge : mapViewer.getRoute()) {
			if (edge.getPoints().size() == 1) {
				edges.remove(edge);
			}
		}
		mapViewer.setRoute(edges);
		calcAllPoints();
	}

	private void optimizeEdges() {

		calcAllPoints();
		List<Edge> edges = mapViewer.getRoute();
		List<Edge> commonEdges = new ArrayList<>();
		for (int i = 0; i < edges.size(); i++) {

			HighResEdge updatedEdgeRef = new HighResEdge(edges.get(i));
			HighResEdge commonEdge = new HighResEdge();
			for (Double[] refPoint : edges.get(i).getPoints()) {
				boolean flagNear = false;
				for (int j = i + 1; j < edges.size(); j++) {
					HighResEdge updatedEdge = new HighResEdge(edges.get(j));
					for (Double[] point : edges.get(j).getPoints()) {
						if (!isNear(refPoint, point)) {
							updatedEdge.addGhPosition(new Double[] { point[1], point[0] });
						} else {
							flagNear = true;
						}
					}
					mapViewer.updateEdge(edges.get(j), updatedEdge);

				}
				if (flagNear) {
					commonEdge.addGhPosition(new Double[] { refPoint[1], refPoint[0] });
				} else {
					updatedEdgeRef.addGhPosition(new Double[] { refPoint[1], refPoint[0] });
				}
			}
			System.out.println("commonEdge: " + commonEdge.getPoints().size());
			commonEdges.add(commonEdge);
			mapViewer.updateEdge(edges.get(i), updatedEdgeRef);
			calcAllPoints();
		}
		mapViewer.addEdges(commonEdges);
		calcAllPoints();
		
		for (Edge edge : mapViewer.getRoute()) {
			if (edge.getPoints().size() < 5) {
				edges.remove(edge);
			}
		}
		mapViewer.setRoute(edges);
		calcAllPoints();
	}

	private boolean isNear(Double[] refPoint, Double[] point) {
		final double DISTANCE = 0.0005;
		double deltaX = Math.abs(refPoint[0] - point[0]);
		double deltaY = Math.abs(refPoint[1] - point[1]);
		if ((deltaX + deltaY) < DISTANCE) {
			return true;
		} else {
			return false;
		}
	}

	private void calcAllPoints() {
		int i = 0;
		List<Edge> edges = mapViewer.getRoute();
		for (Edge edge : edges) {
			i += edge.getPoints().size();
		}
		System.out.println(i + " Points - " + edges.size() + " Edges");

	}

	private void mapEdgesToStreets() {
		for (Edge edge : mapViewer.getRoute()) {
			pool.invoke(new ForkJoinTask<Edge>() {

				private static final long serialVersionUID = 1475668164109020735L;

				@Override
				public Edge getRawResult() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				protected void setRawResult(Edge value) {
					// TODO Auto-generated method stub

				}

				@Override
				protected boolean exec() {
					HighResEdge highResEdge = getHighRes(edge);
					System.out.println("DONE");
					return mapViewer.updateEdge(edge, highResEdge);
				}
			});
		}
	}

	private HighResEdge getHighRes(Edge edge) {
		GHPoint start = new GHPoint(edge.getStart().getLatitude(), edge.getStart().getLongitude());
		GHPoint dest = new GHPoint(edge.getDest().getLatitude(), edge.getDest().getLongitude());

		GHRequest ghRequest = new GHRequest(start, dest);
		GHResponse response = graphHopper.route(ghRequest);

		PathWrapper path = response.getBest();
		PointList points = path.getPoints();

		HighResEdge highResEdge = new HighResEdge(edge);

		highResEdge.addGhPositions(points.toGeoJson());
		return highResEdge;
	}

	private void initGraphhopper() {
		graphHopper = new GraphHopperOSM().forDesktop();

		CarFlagEncoder encoder = new CarFlagEncoder();
		graphHopper.setEncodingManager(new EncodingManager(encoder));
		graphHopper.getCHFactoryDecorator().setEnabled(false);

		long time = System.currentTimeMillis();
		System.out.println("Start init GH");
		try {
			CmdArgs args = CmdArgs.readFromConfig("src/main/resources/graphhopper/config.properties",
					"graphhopper.config");
			graphHopper.init(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graphHopper.importOrLoad();
		System.out.println("End init GH - " + (System.currentTimeMillis() - time + " ms"));
	}
}
