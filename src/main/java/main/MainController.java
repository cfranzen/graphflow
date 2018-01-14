package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;

import gui.MainFrame;
import gui.MyMap;
import gui.TextAreaOutputStream;
import models.Constants;
import models.Edge;
import models.ModelLoader;
import newVersion.main.Optimizer;
import newVersion.main.WaypointController;
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

	private MyMap mapViewer;
	private ModelLoader input;
	private GraphHopper graphHopper;
	private int currentTime = 0;

	private RouteController routeController;
	private WaypointController waypointController;
	private SeaController seaController;

	private MainFrame mainFrame;

	/**
	 * used to search for contact points with other edges, distance in lat/lon
	 * not pixel
	 */
	public final static double contactSearchDistance = 0.021;
	// /**
	// * used for removing multiple point which are near together, distance in
	// * lat/lon not pixel
	// */
	// public final static double reduceEdgeDistance = 0.01075;
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
		initializeLogging();
		cliInput = new cliInput();
	}

	/**
	 * Main method.
	 */
	private void run() {

		writeInputToConstants();
		initControllers();
		initGui();

		// Load sea data
		seaController = SeaController.getInstance(mapViewer);
		seaController.loadSeaNodes(cliInput.seaNodes);

		// Processing input to own classes
		loadSolution();

		routeController.importEdges(input.edges);
		routeController.getAllRoutes();
		waypointController.createWaypointsFromGeo(input.nodes);
		if (Constants.zoomAggregation) {
			Optimizer.aggregateWaypoints(routeController, waypointController);
		}

		mapViewer.setWaypoints(new HashSet<>(waypointController.getWaypoints(mapViewer.getZoom())));
		mainFrame.maxTimeSlider(input.timesteps * Constants.PAINT_STEPS_COUNT);

		optimize();
	}

	private void initControllers() {
		routeController = new RouteController();
		waypointController = new WaypointController();
	}

	private void writeInputToConstants() {
		if (cliInput.zoomLevel != 0) {
			Constants.ZOOM_LEVEL_COUNT = cliInput.zoomLevel;
		}
		if (cliInput.timeStepDelay != 0) {
			Constants.TIME_STEP_DELAY = cliInput.timeStepDelay;
		}
		if (cliInput.paintStepCount != 0) {
			Constants.PAINT_STEPS_COUNT = cliInput.paintStepCount;
		}
		Constants.zoomAggregation = cliInput.zoomAggregation;
		Constants.LOGGER_LEVEL = Level.toLevel(cliInput.logLevel, Constants.LOGGER_LEVEL);
	}

	private void loadSolution() {
		File f = new File(cliInput.modelFilePath);
		if (f.exists() && !f.isDirectory()) {
			input = ModelLoader.loadFile(cliInput.modelFilePath, cliInput.solutionFilePath);
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
		incTime(Constants.PAINT_STEPS_COUNT);
	}

	private void incTime(int steps) {
		currentTime += steps;
		if (currentTime >= input.timesteps * Constants.PAINT_STEPS_COUNT) {
			currentTime = 0;
		}
		if (Constants.debugInfos && currentTime > Constants.MAX_TIME_STEPS * Constants.PAINT_STEPS_COUNT) {
			currentTime = 0;
		}
		if (currentTime % Constants.PAINT_STEPS_COUNT == 0 && Constants.showTimesteps) {
			logger.info("Timestep: " + currentTime / Constants.PAINT_STEPS_COUNT);
		}

		setTime(currentTime);
	}

	public void setTime(int value) {
		currentTime = value;
		mapViewer.setTime(currentTime);
		if (mainFrame != null) {
			mainFrame.updateTimeText(currentTime);
		}
	}

	public void reduceTime() {
		currentTime -= Constants.PAINT_STEPS_COUNT;
		if (currentTime <= 0) {
			currentTime = input.timesteps;
		}
		setTime(currentTime);
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
		if (Constants.LOGGER_LEVEL != Level.OFF) {
			createLoggerFrame();
		}

		mapViewer = new MyMap(this, routeController, waypointController);

		mainFrame = new MainFrame(this, mapViewer);
		mainFrame.setResizable(true);
		mainFrame.setVisible(true);

		routeController.addPropertyChangeListener(mapViewer);
	}

	private void createLoggerFrame() {
		JFrame frame = new JFrame("Logger Output");
		JTextArea ta = new JTextArea();
		TextAreaOutputStream taos = new TextAreaOutputStream(ta, 60);
		PrintStream ps = new PrintStream(taos);
		System.setOut(ps);
		System.setErr(ps);
		frame.add(new JScrollPane(ta));
		frame.pack();
		frame.setVisible(true);
		frame.setSize(800, 600);
		LogManager.getRootLogger().addAppender(taos.getAppender());
	}

	private void optimize() {
		// init GH
		long time = System.currentTimeMillis();
		logger.info("Start GH");
		initGraphhopper();

		// own thread so that the main thread is not blocked
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Optimizer optimizer = new Optimizer();
				optimizer.mapEdgesToStreets(graphHopper, routeController, seaController);
				repaint();
				// optimize GH edges
				optimizer.reducePointCount(routeController);
				repaint();
				logger.info("End GH - " + (System.currentTimeMillis() - time));
				if (Constants.OPTIMIZE) {
					optimizer.optimize(routeController, waypointController);
				}
				finishedEdgeOptimizing();
			}
		});
		t.start();

	}

	private void finishedEdgeOptimizing() {
		mapViewer.setWaypoints(new HashSet<>(waypointController.getWaypoints(mapViewer.getZoom())));
		repaint();
		mainFrame.updateProgessBar(100);
		logger.info("Finished edge optimizing");
		logger.info("Try to save current edge state to working directory");
		saveCurrentEdgeState();
		logger.info("Running Completed");
	}

	/**
	 * Saves the current {@link Edge} state to the working directory. Also saves
	 * a footprint of the input files for recognizing and reloading.
	 */
	private void saveCurrentEdgeState() {
		String savenameRouteController = "routeControllerSave_";
		
// Streaming
//		JsonWriter writer;
//		writer = new JsonWriter(new FileWriter(savenameRouteController));
//		writer.beginObject();
//		
//		writer.
//		
//		writer.endObject();
		
		
		Gson gson = new Gson();
		
		
		List<List<Edge>> routes = routeController.getAllRoutes();
		
//		for (List<Edge> route : routeController.getAllRoutes()) {
			for (int i = 0; i < routes.size(); i++) {
				List<Edge> route = routes.get(i);
				
				String json = gson.toJson(route);
		
		
		try (FileWriter writer = new FileWriter(new File(savenameRouteController + i +".json" ))){
			writer.write(json);
		} catch (IOException e) {
			logger.error("State could not be saved");
			e.printStackTrace();
		} }
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
