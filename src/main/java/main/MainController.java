package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import models.Constants;
import models.Edge;
import models.EdgeType;
import models.HighResEdge;
import models.MapRoute;
import models.MapRoute.MapPoint;
import models.ModelLoader;
import sea.SeaController;

/**
 * Main class from the program
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	private static MainController controller;

	public static void main(String[] args) {
		MainController controller = getInstance();
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

	public cliInput cliInput;

	private ForkJoinPool pool;
	private MyMap mapViewer;
	private ModelLoader input;
	private GraphHopper graphHopper;
	private int currentTime = 0;

	private JFrame frame;

	private RouteController routeController = RouteController.getInstance();
	private SeaController seaController = SeaController.getInstance();

	/**
	 * used to search for contact points with other edges, distance in lat/lon
	 * not pixel
	 */
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
	public final static double combinePointsDistance = 0.01;

	/**
	 * Returns the {@link MainController} instance, if the instance is
	 * <code>null</code> a new instance is created.
	 * 
	 * @return the {@link MainController} instance
	 */
	public static MainController getInstance() {
		if (controller == null) {
			controller = new MainController();
		}
		return controller;
	}

	/**
	 * Private constructor to override the default and ensure the singleton
	 * pattern.
	 */
	private MainController() {
		pool = new ForkJoinPool();
		initializeLogging();
		cliInput = new cliInput();
	}

	/**
	 * Main method.
	 */
	public void run() {

		initGui();

		// Load sea data
		seaController.loadSeaNodes(cliInput.seaNodes);

		// Processing input to own classes
		loadSolution();

		optimize();

	}

	private void loadSolution() {
		File f = new File(cliInput.modelFilePath);
		if (f.exists() && !f.isDirectory()) {
			input = ModelLoader.loadFile(cliInput.modelFilePath, cliInput.solutionFilePath);
			mapViewer.addPositions(input.nodes);
			routeController.addEdges(input.edges);
		}

	}

	/**
	 * Increases the current time step each time its called. If the maximum time
	 * step from the model is reached it resets the step to zero.
	 */
	public void incPaintTime() {
		incTime(1);
	}

	public void incTime() {
		incTime(Constants.PAINT_STEPS);
	}

	private void incTime(int steps) {
		currentTime += steps;
		if (currentTime >= input.timesteps * Constants.PAINT_STEPS) {
			currentTime = 0;
		}
		if (currentTime > 25 * Constants.PAINT_STEPS) { // XXX DEBUG
			currentTime = 0;
		}
		if (currentTime % Constants.PAINT_STEPS == 0) {
			System.out.println("Timestep: " + currentTime / Constants.PAINT_STEPS);
		}

		mapViewer.setTime(currentTime);
	}

	/**
	 * 
	 */
	protected void reduceTime() {
		currentTime -= Constants.PAINT_STEPS;
		if (currentTime <= 0) {
			currentTime = input.timesteps;
		}
		System.out.println("Timestep: " + currentTime / Constants.PAINT_STEPS);
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

	private void initializeLogging() {
		try {
			DOMConfigurator.configure(getClass().getResource("/log4j.xml"));
		} catch (Exception e) {
			System.err.println("FATAL: log configuration failed: " + e);
			e.printStackTrace();
		}
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

		JButton plusBtn = new JButton();
		plusBtn.setText("+");
		plusBtn.setSize(80, 25);
		plusBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainController.getInstance().incTime();
			}
		});
		layeredPane.add(plusBtn, new Integer(30));
		JButton minusBtn = new JButton();
		minusBtn.setText("-");
		minusBtn.setSize(40, 25);
		minusBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainController.getInstance().reduceTime();
			}
		});

		layeredPane.add(minusBtn, new Integer(40));

		// Add input to viewer
		mapViewer = new MyMap(this, routeController);

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

		// own thread so that the gui thread is not blocked
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				optimizeEdges();
			}
		});
		t.start();

	}

	/**
	 * @param factor
	 */
	private void reducePointCount(int factor) {
		logger.info("Reduce point resolution per edge; before - after");
		sumAllPoints();
		List<Edge> edges = routeController.getRoute();
		if (edges.get(0) instanceof MapRoute) {
			for (Edge edge : edges) {
				MapRoute mapEdge = (MapRoute) edge;
				routeController.updateEdge(mapEdge, reduceEdgePoints(mapEdge, factor));
			}
		} else {
			for (Edge edge : edges) {
				routeController.updateEdge(edge, reduceEdgePoints(edge, factor));
			}
		}
		repaint();
		sumAllPoints();

	}

	private void reducePointCount() {
		reducePointCount(1);
	}

	private void optimizeEdges() {
		sumAllPoints();
		logger.info("optimize");

		List<Edge> savedEdges = combineEdges();

		// List<Edge> savedEdges = splitEdges();

		/*
		 * Find nearest contact point. TODO connect to thickest part instead of
		 * nearest. Maybe get Edge from point. </br>Every edge has to be
		 * connected to either an other edge or a waypoint.
		 */
		reducePointCount(3);
		logger.info("connect edge end points");
		// connectEndpoints(savedEdges); // TODO optimize is buggy
		routeController.setRoute(savedEdges);
		sumAllPoints();
		logger.info("optimize-end");
	}

	private void connectEndpoints(List<Edge> savedEdges) {
		logger.debug("Connect waypoints/edges to edges");
		List<GeoPosition> waypoints = mapViewer.getWaypoints();
		for (int j = 0; j < savedEdges.size(); j++) {
			logger.info("Processed " + j + " from " + savedEdges.size());
			Edge edge = savedEdges.get(j);
			List<MapPoint> points = ((MapRoute) edge).getPoints();
			MapPoint mpFirst = points.get(0);
			MapPoint mpLast = points.get(points.size() - 1);

			boolean flagFirst = false;
			boolean flagLast = false;
			for (GeoPosition waypoint : waypoints) {
				if (isNear(mpFirst.getPosition(), waypoint, contactSearchDistance)) {
					flagFirst = true;
					break;
				}
			}
			if (!flagFirst) {
				getContactForPoint(savedEdges, j, mpFirst);
				edge.setStart(mpFirst.contactPoint);
				continue;
			}
			for (GeoPosition waypoint : waypoints) {
				if (isNear(mpLast.getPosition(), waypoint, contactSearchDistance)) {
					flagLast = true;
					break;
				}
			}
			if (!flagLast) {
				getContactForPoint(savedEdges, j, mpLast);
				edge.setDest(mpFirst.contactPoint);
			}
		}
	}

	private List<Edge> splitEdges() {
		/*
		 * Split partial Edges.
		 * 
		 * TODO Acces to saved Edges should be synchronized so that this step
		 * can be parallelized.
		 */
		logger.info("Split partial edges into multiple");
		List<Edge> savedEdges = Collections.synchronizedList(routeController.getRoute());

		// List<Edge> refEdges = null;
		// //XXX
		// RecursiveTask<List<Edge>> task = new RecursiveTask<List<Edge>>() {
		//
		//
		// @Override
		// protected List<Edge> compute() {
		//
		// return null;
		// }
		// };
		//

		// Wenn zwei Punkte viel zu weit auseinander liegen wurde die Kante in
		// der Mitte mit einer anderen zusammengelegt
		for (int i = 0; i < savedEdges.size(); i++) {
			List<MapPoint> points = ((MapRoute) savedEdges.get(i)).getPoints();
			GeoPosition last = points.get(0).getPosition();

			for (int j = 1; j < points.size(); j++) {
				GeoPosition current = points.get(j).getPosition();
				if (getDistance(last, current) > 1.2) {
					logger.info("Split this edge into two -> " + i + "-" + j);
					logger.info(last + " - " + current + " - " + getDistance(last, current));
					MapRoute mapEdgeOld = new MapRoute();
					for (MapRoute.MapPoint mapPoint : points.subList(0, j)) {
						mapEdgeOld.addPoint(mapPoint);
					}
					MapRoute mapEdgeNew = new MapRoute();
					for (MapPoint mapPoint : points.subList(j, points.size())) {
						mapEdgeNew.addPoint(mapPoint);
					}
					synchronized (savedEdges) {
						savedEdges.set(i, mapEdgeOld);
						savedEdges.add(mapEdgeNew);
						// TODO new taks for new edge for parallel progession
					}
				}
				last = current;
			}
		}
		logger.info("--> splitted in " + savedEdges.size() + " edges");
		return savedEdges;
	}

	private List<Edge> combineEdges() {
		// combine near points from multiple edges into one
		// uses multiple edges at once -> linear
		sumAllPoints();
		List<Edge> edges = routeController.getRoute();
		List<Edge> savedEdges = new ArrayList<>();
		for (int i = 0; i < edges.size(); i++) {
			MapRoute mapEdge = new MapRoute();
			for (GeoPosition point : edges.get(i).getPositions()) {
				if (!hasAnyNearPoint(point, savedEdges)) {
					mapEdge.addNewPoint(point, edges);
				} // if point is close to an already saved point, do nothing
			}
			if (mapEdge.getPositions().size() > 3) {
				mapEdge.updateStartEnd();
				savedEdges.add(mapEdge);
				logger.info("Added combined edge; Count: " + savedEdges.size());
			}
			logger.info("Edge " + i + " from " + edges.size() + " processed");
		}
		logger.info("--> combined to " + savedEdges.size() + " edges");
		// updateView
		routeController.setRoute(savedEdges);
		return savedEdges;
	}

	private void getContactForPoint(List<Edge> savedEdges, int j, MapPoint mp) {
		for (int i = 0; i < savedEdges.size(); i++) {
			// So that the edge do not connect to the same edge, the edge list
			// has to be ordered to function.
			if (i == j) {
				continue;
			}
			double calcDistRef = contactSearchDistance;
			for (int k = 0; k < savedEdges.get(i).getPositions().size(); k++) {

				GeoPosition point = savedEdges.get(i).getPositions().get(k);
				double calcDist = getDistance(mp.getPosition(), point);
				// iterate through all points to find the nearest point
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
	public static Map<Edge, GeoPosition> getNearEdges(GeoPosition refPoint, List<Edge> edges, double distance) {
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
	 * Returns if the given {@link GeoPosition} has any near point in the given
	 * {@link List} of {@link Edge}s.
	 * 
	 * @param refPoint
	 *            point to check
	 * @param savedEdges
	 *            {@link List} of {@link Edge}s which points are to compare
	 * @return <code>true</code> if the point has a near point </br>
	 *         <code>false</code> otherwise
	 */
	private boolean hasAnyNearPoint(GeoPosition refPoint, List<Edge> savedEdges) {
		for (int i = 0; i < savedEdges.size(); i++) {
			for (GeoPosition point : savedEdges.get(i).getPositions()) {
				if (isNear(refPoint, point, combinePointsDistance)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if an {@link Edge} has multiple nearby points and only leaves one
	 * point at this position.
	 * 
	 * Create new Edge with old parameters. Iterate trough all points from the
	 * old edge but add the current point only if it do not have any near points
	 * in the new savedPoint list.
	 * 
	 * @param edge
	 * @return
	 */
	private Edge reduceEdgePoints(Edge refEdge, int factor) {
		HighResEdge edge = new HighResEdge(refEdge);
		for (GeoPosition point : refEdge.getPositions()) {
			boolean flag = true;
			for (GeoPosition savedPoint : edge.getPositions()) {
				if (isNear(point, savedPoint, factor * reduceEdgeDistance)) {
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

	private Edge reduceEdgePoints(MapRoute mapedge, int factor) {
		MapRoute edge = new MapRoute(mapedge);
		for (MapPoint point : mapedge.getPoints()) {
			boolean flag = true;
			for (GeoPosition savedPoint : edge.getPositions()) {
				if (isNear(point.getPosition(), savedPoint, factor * reduceEdgeDistance)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				edge.addPoint(point);
			}
		}
		return edge;
	}

	private static boolean isNear(GeoPosition refPoint, GeoPosition point, double distance) {
		if (getDistance(refPoint, point) < distance) {
			return true;
		} else {
			return false;
		}
	}

	private static double getDistance(GeoPosition refPoint, GeoPosition point) {
		double deltaX = Math.abs(refPoint.getLatitude() - point.getLatitude());
		double deltaY = Math.abs(refPoint.getLongitude() - point.getLongitude());
		return deltaX + deltaY;
	}

	private void sumAllPoints() {
		int i = 0;
		List<Edge> edges = routeController.getRoute();
		for (Edge edge : edges) {
			i += edge.getPositions().size();
		}
		logger.info(i + " Points - " + edges.size() + " Edges");

	}

	private void mapEdgesToStreets() {
		List<Edge> land = routeController.getRoute();
		List<Edge> sea = routeController.getSeaRoute();
		List<Edge> route = new ArrayList<>(land.size() + sea.size());
		route.addAll(land);
		route.addAll(sea);

		for (Edge edge : route) {
			pool.invoke(new ForkJoinTask<Edge>() {

				private static final long serialVersionUID = 1475668164109020735L;

				@Override
				public Edge getRawResult() {
					// noop
					return null;
				}

				@Override
				protected void setRawResult(Edge value) {
					// noop
				}

				@Override
				protected boolean exec() {
					HighResEdge highResEdge;
					if (edge.getType().equals(EdgeType.VESSEL)) {

						DijkstraAlgorithm dijkstraAlgorithm;
						dijkstraAlgorithm = new DijkstraAlgorithm(seaController.getEdges());
						dijkstraAlgorithm.execute(edge.getStart());
						List<GeoPosition> steps = dijkstraAlgorithm.getPath(edge.getDest());
						highResEdge = new HighResEdge(edge);
						highResEdge.addPositions(steps);
						logger.info("map edge to sea route DONE - " + steps.size());
						if (highResEdge != null) {
							routeController.updateSeaEdge(edge, highResEdge);
						}
					} else {
						highResEdge = getHighRes(edge);
						logger.info("map edge to street DONE");
						if (highResEdge != null) {
							routeController.updateEdge(edge, highResEdge);
						}
					}
					repaint();
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

		if (response.hasErrors() || response.getAll().isEmpty()) {
			logger.info("Could not find solution for edge with start: " + edge.getStart().toString());
			return null;
		}
		PathWrapper path = response.getBest();
		PointList points = path.getPoints();

		// XXX Instructions contain GPX Data, for saving the optimized graph see
		// Method:
		// path.getInstructions().createGPX()

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
			logger.error("Could not read graphopper files");
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		graphHopper.importOrLoad();
		logger.info("End init GH - " + (System.currentTimeMillis() - time + " ms"));
	}

	/**
	 * @return
	 */
	public SeaController getSeaController() {
		return seaController;
	}

	/**
	 * 
	 */
	public void repaint() {
		mapViewer.repaint();
	}
}
