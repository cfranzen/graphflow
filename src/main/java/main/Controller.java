package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.apache.log4j.xml.DOMConfigurator;
import org.jxmapviewer.viewer.GeoPosition;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private static Controller controller;

	public static void main(String[] args) {

		/*
		 * osm file path, gh solution file
		 */
		Controller controller = getInstance();

		CmdLineParser parser = new CmdLineParser(controller.cliInput);
		try {
			parser.parseArgument(args);
			controller.run();
		} catch (CmdLineException e) {
			// handling of wrong arguments
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
		}
	}

	private void initializeLogging() {
		try {
			DOMConfigurator.configure(getClass().getResource("/log4j.xml"));
		} catch (Exception e) {
			System.err.println("FATAL: log configuration failed: " + e);
			e.printStackTrace();
		}
	}

	public cliInput cliInput;

	private ForkJoinPool pool;
	private MyMap mapViewer;
	private ModelLoader input;
	private GraphHopper graphHopper;
	private int currentTime = 0;

	private JFrame frame;

	/**
	 * used to search for contact points with other edges, distance in lat/lon
	 * not pixel
	 */
	// public final static double contactSearchDistance = 0.025;
	public final static double contactSearchDistance = 0.021;
	/**
	 * used for removing multiple point which are near together, distance in
	 * lat/lon not pixel
	 */
	public final static double reduceEdgeDistance = 0.0075;
	/**
	 * used for combining points and summarizing workload and capacity, distance
	 * in lat/lon not pixel
	 */
	public final static double combinePointsDistance = 0.005;

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
		initializeLogging();
		cliInput = new cliInput();
	}

	/**
	 * Main method.
	 * 
	 * TODO distinct gui creation
	 */
	public void run() {
		// Processing input to own classes
		input = ModelLoader.loadFile(cliInput.modelFilePath, cliInput.solutionFilePath);

		initGui();
		optimize();
	}

	private void initGui() {
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

		logger.debug(frame.getSize().toString());
		// layeredPane.setSize(frame.getSize());
		logger.debug(layeredPane.getSize().toString());

		JButton btn = new RunButton(this);
		btn.setSize(150, 50);
		layeredPane.add(btn, new Integer(20));
		JButton resetBtn = new JButton();
		resetBtn.setText("RESET");
		resetBtn.setSize(10, 50);
		resetBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});
		layeredPane.add(resetBtn, new Integer(30));

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
	}

	private void optimize() {
		// init GH
		long time = System.currentTimeMillis();
		logger.info("Start GH");
		initGraphhopper();
		// optimize GH edges
		mapEdgesToStreets();
		logger.info("End GH - " + (System.currentTimeMillis() - time));

		reducePointCount();

		optimizeEdges();

		// XXX For Debug
		mapViewer.setZoom(13);
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
		logger.info("optimize");
		sumAllPoints();
		List<Edge> edges = mapViewer.getRoute();
		List<Edge> savedEdges = new ArrayList<>();
		for (int i = 0; i < edges.size(); i++) {
			MapEdge mapEdge = new MapEdge();
			for (GeoPosition point : edges.get(i).getPositions()) {
				if (!hasAnyNearPoint(point, savedEdges)) {
					MapPoint mp = new MapPoint(point, getNearEdges(point, edges, combinePointsDistance));
					mapEdge.addPoint(mp);
				}
			}
			if (mapEdge.getPositions().size() > 1) {
				savedEdges.add(mapEdge);
			}
		}
		/*
		 * Split partial Edges
		 */
		for (int i = 0; i < savedEdges.size(); i++) {
			List<MapPoint> points = ((MapEdge) savedEdges.get(i)).getPoints();
			GeoPosition last = points.get(0).getPosition();

			for (int j = 1; j < points.size(); j++) {
				GeoPosition current = points.get(j).getPosition();
				if (getDistance(last, current) > contactSearchDistance * 4) {
					logger.info("Split this edge into two -> " + i + "-" + j);
					logger.info(last.getLatitude() + " - " + last.getLongitude());
					logger.info(current.getLatitude() + " - " + current.getLongitude());
					MapEdge mapEdgeOld = new MapEdge();
					for (MapPoint mapPoint : points.subList(0, j)) {
						mapEdgeOld.addPoint(mapPoint);
					}
					savedEdges.set(i, mapEdgeOld);

					MapEdge mapEdgeNew = new MapEdge();
					for (MapPoint mapPoint : points.subList(j, points.size())) {
						mapEdgeNew.addPoint(mapPoint);
					}
					savedEdges.add(mapEdgeNew);
				}
				last = current;
			}
		}

		/*
		 * Find nearest contact point. TODO connect to thickest part instead of
		 * nearest.
		 */
		List<GeoPosition> waypoints = mapViewer.getWaypoints();
		for (int j = 0; j < savedEdges.size(); j++) {
			Edge edge = savedEdges.get(j);
			List<MapPoint> points = ((MapEdge) edge).getPoints();
			MapPoint mpFirst = points.get(0);
			MapPoint mpLast = points.get(points.size() - 1);

			boolean flagFirst = false;
			boolean flagLast = false;
			for (GeoPosition waypoint : waypoints) {
				if (isNear(mpFirst.getPosition(), waypoint, combinePointsDistance)) {
					flagFirst = true;
					break;
				}
			}
			for (GeoPosition waypoint : waypoints) {
				if (isNear(mpLast.getPosition(), waypoint, combinePointsDistance)) {
					flagLast = true;
					break;
				}
			}
			if (!flagFirst) {
				getContactForPoint(savedEdges, j, mpFirst);
			}
			if (!flagLast) {
				getContactForPoint(savedEdges, j, mpLast);
			}
		}
		sumAllPoints();
		mapViewer.setRoute(savedEdges);
		sumAllPoints();
		logger.info("optimize-end");
	}

	private void getContactForPoint(List<Edge> savedEdges, int j, MapPoint mp) {
		for (int i = 0; i < savedEdges.size(); i++) {
			if (i == j) {
				continue;
			}
			double calcDistRef = contactSearchDistance;
			for (int k = 0; k < savedEdges.get(i).getPositions().size(); k++) {

				GeoPosition point = savedEdges.get(i).getPositions().get(k);
				double calcDist = getDistance(mp.getPosition(), point);
				if (calcDist < calcDistRef) {
					calcDistRef = calcDist;
					mp.contactPoint = point;
				}
			}
		}
	}

	/**
	 * @param point
	 * @return
	 */
	private Map<Edge, GeoPosition> getNearEdges(GeoPosition refPoint, List<Edge> edges, double distance) {
		Map<Edge, GeoPosition> nearEdges = new HashMap<>();

		for (int i = 0; i < edges.size(); i++) {
			for (GeoPosition point : edges.get(i).getPositions()) {
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
			for (GeoPosition point : savedEdges.get(i).getPositions()) {
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
		HighResEdge edge = new HighResEdge(refEdge);
		for (GeoPosition point : refEdge.getPositions()) {
			boolean flag = true;
			for (GeoPosition savedPoint : edge.getPositions()) {
				if (isNear(point, savedPoint, reduceEdgeDistance)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				edge.getPositions().add(point);
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
			i += edge.getPositions().size();
		}
		logger.info(i + " Points - " + edges.size() + " Edges");

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
					logger.info("optimize edge DONE");
					if (highResEdge != null) {
						mapViewer.updateEdge(edge, highResEdge);
					}
					return true;
				}
			});
		}
	}

	private HighResEdge getHighRes(Edge edge) {
		GHPoint start = new GHPoint(edge.getStart().getLatitude(), edge.getStart().getLongitude());
		GHPoint dest = new GHPoint(edge.getDest().getLatitude(), edge.getDest().getLongitude());

		GHRequest ghRequest = new GHRequest(start, dest);
		GHResponse response = graphHopper.route(ghRequest);

		if (response.getAll().isEmpty()) {
			return null;
		}
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
		logger.info("Start init GH");
		try {
			CmdArgs args = CmdArgs.readFromConfig(cliInput.ghFolder + "config.properties", "graphhopper.config");
			args.put("datareader.file", cliInput.osmFilePath);
			args.put("graph.location", cliInput.ghFolder);
			graphHopper.init(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graphHopper.importOrLoad();
		logger.info("End init GH - " + (System.currentTimeMillis() - time + " ms"));
	}
}
