package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.jxmapviewer.viewer.GeoPosition;

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
import models.MapEdge;
import models.MapPoint;
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

		reducePointCount();

		optimizeEdges();
	}

	private void reducePointCount() {
		sumAllPoints();
		List<Edge> edges = mapViewer.getRoute();
		for (Edge edge : edges) {
			mapViewer.updateEdge(edge, reduceEdgePoints(edge));
		}
		sumAllPoints();
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

	private void optimizeEdges() {
		System.out.println("optimize");
		sumAllPoints();
		List<Edge> edges = mapViewer.getRoute();
		List<Edge> savedEdges = new ArrayList<>();
		for (int i = 0; i < edges.size(); i++) {
			MapEdge mapEdge = new MapEdge();
			for (GeoPosition point : edges.get(i).getPoints()) {
				if (hasAnyNearPoint(point, savedEdges)) {
					// TODO commonEdge?
				} else {
					double distance = 0.01;
					MapPoint mp = new MapPoint(point, getNearEdges(point, edges, distance));
					mapEdge.points.add(mp);
				}
			}
			if (mapEdge.points.size() > 5) {
				savedEdges.add(mapEdge);
			}
		}

		/*
		 * Find nearest contact point. TODO connect to thickest part instead of
		 * nearest.
		 */
		// for (Edge edge : savedEdges) {
		for (int j = 0; j < savedEdges.size(); j++) {
			Edge edge = savedEdges.get(j);
			List<MapPoint> points = ((MapEdge) edge).points;
			MapPoint mpFirst = points.get(0);
			MapPoint mpLast = points.get(points.size() - 1);
			double distance = 0.02;

			for (int i = 0; i < savedEdges.size(); i++) {
				if (i == j) {
					continue;
				}
				for (GeoPosition point : savedEdges.get(i).getPoints()) {
					if (isNear(mpFirst.getPosition(), point, distance)) {
						if (mpFirst.contactPoint == null || getDistance(mpFirst.getPosition(),
								point) < getDistance(mpFirst.getPosition(), mpFirst.contactPoint)) {
							mpFirst.contactPoint = point;
						}
					}
					if (isNear(mpLast.getPosition(), point, distance)) {
						if (mpLast.contactPoint == null || getDistance(mpLast.getPosition(),
								point) < getDistance(mpLast.getPosition(), mpLast.contactPoint)) {
							mpLast.contactPoint = point;
						}
					}
				}
			}
		}
		sumAllPoints();
		mapViewer.setRoute(savedEdges);
		sumAllPoints();
		System.out.println("optimize-end");
	}

	/**
	 * @param point
	 * @return
	 */
	private Map<Edge, GeoPosition> getNearEdges(GeoPosition refPoint, List<Edge> edges, double distance) {
		Map<Edge, GeoPosition> nearEdges = new HashMap<>();

		for (int i = 0; i < edges.size(); i++) {
			for (GeoPosition point : edges.get(i).getPoints()) {
				if (isNear(refPoint, point, distance)) {
					nearEdges.put(edges.get(i), point);
					continue;
				}
			}
		}
		return nearEdges;
	}

	/**
	 * @param point
	 * @param savedEdges
	 */
	private boolean hasAnyNearPoint(GeoPosition refPoint, List<Edge> savedEdges) {
		double distance = 0.01;
		for (int i = 0; i < savedEdges.size(); i++) {
			for (GeoPosition point : savedEdges.get(i).getPoints()) {
				if (isNear(refPoint, point, distance)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param edge
	 * @return
	 */
	private Edge reduceEdgePoints(Edge refEdge) {
		double distance = 0.0075;
		HighResEdge edge = new HighResEdge(refEdge);
		for (GeoPosition point : refEdge.getPoints()) {
			boolean flag = true;
			for (GeoPosition savedPoint : edge.getPoints()) {
				if (isNear(point, savedPoint, distance)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				edge.getPoints().add(point);
			}
		}
		return edge;
	}

	private boolean isNear(GeoPosition refPoint, GeoPosition point, double distance) {
		if (getDistance(refPoint, point) < distance) {
			return true;
		} else {
			return false;
		}
	}

	private double getDistance(GeoPosition refPoint, GeoPosition point) {
		double deltaX = Math.abs(refPoint.getLatitude() - point.getLatitude());
		double deltaY = Math.abs(refPoint.getLongitude() - point.getLongitude());
		return deltaX + deltaY;
	}

	private void sumAllPoints() {
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
