package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

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

import gui.Map;
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

	private Map mapViewer;
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
		mapViewer = new Map(this);
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
	public Map getMapViewer() {
		return mapViewer;
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
							updatedEdge.addPosition(new Double[] { point[1], point[0] });
						} else {
							flagNear = true;
						}
					}

					mapViewer.updateEdge(edges.get(j), updatedEdge);

				}
				if (flagNear) {
					commonEdge.addPosition(new Double[] { refPoint[1], refPoint[0] });
				} else {
					updatedEdgeRef.addPosition(new Double[] { refPoint[1], refPoint[0] });
				}
			}
			System.out.println("commonEdge: " + commonEdge.getPoints().size());
			commonEdges.add(commonEdge);
			mapViewer.updateEdge(edges.get(i), updatedEdgeRef);
			calcAllPoints();
		}
		mapViewer.addEdges(commonEdges);
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
		System.out.println(i + " Points");

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

		// --
		// List<Double[]> pointList = points.toGeoJson();
		// highResEdge.addPosition(pointList.get(0));
		// for (int i = 10; i < pointList.size() - 100; i += 100) {
		// highResEdge.addPosition(pointList.get(i));
		// }
		// if (pointList.size() > 1) {
		// highResEdge.addPosition(pointList.get(pointList.size() - 1));
		// }
		// Uses 50 MB more RAM for Germany-example than code above, see
		// optimizeEdges-function
		highResEdge.addPositions(points.toGeoJson());
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
