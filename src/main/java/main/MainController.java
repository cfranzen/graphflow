package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

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
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;

import dto.RouteControllerDTO;
import gui.MainFrame;
import gui.MyMap;
import gui.TextAreaOutputStream;
import models.Constants;
import models.Edge;
import models.ModelLoader;
import newVersion.main.Optimizer;
import newVersion.main.PaintController;
import newVersion.main.WaypointController;
import painter.SeaRouteController;
import sea.SeaNodeFactory;

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

	public CliInput cliInput;

	private MyMap mapViewer;
	private ModelLoader input;
	private GraphHopper graphHopper;
	private int currentTime = 0;

	private RouteController routeController;
	private WaypointController waypointController;
	private SeaNodeFactory seaController;

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
		cliInput = new CliInput();
	}

	/**
	 * Main method.
	 */
	private void run() {
		writeInputToConstants();

		if (Constants.LOGGER_LEVEL != Level.OFF) {
			createLoggerFrame();
		}

		initControllers();
		boolean lastLoaded = loadLastRun();
		initGui();

		// Processing input to own classes
		loadSolution();

		Constants.timesteps = input.timesteps;

		// Load sea data
		// List<Edge> seaNodes = 
		SeaNodeFactory.loadSeaNodes(cliInput.seaNodes);

		mainFrame.setVisible(true);

		if (lastLoaded) {
			mapViewer.setWaypoints(new HashSet<>(waypointController.getWaypoints(mapViewer.getZoom())));
			mainFrame.maxTimeSlider(Constants.timesteps * Constants.PAINT_STEPS_COUNT);
			finishedEdgeOptimizing();
			return;
		}
		logger.info("No last run file found or params file is not identical");

		// // Processing input to own classes
		// loadSolution();

		routeController.importEdges(input.edges);
		waypointController.createWaypointsFromGeo(input.nodes);
		if (Constants.zoomAggregation) {
			Optimizer.aggregateWaypoints(routeController, waypointController);
		}

		mapViewer.setWaypoints(new HashSet<>(waypointController.getWaypoints(mapViewer.getZoom())));
		mainFrame.maxTimeSlider(Constants.timesteps * Constants.PAINT_STEPS_COUNT);

		optimize();
	}

	/**
	 * @return
	 */
	private boolean loadLastRun() {
		if (cliInput.clearRun) {
			logger.info("Clear save files");
			try {
				deleteFile(Constants.SAVENAME_PARAMS);
				deleteFile(Constants.SAVENAME_WAYPOINT_CONTROLLER);
				deleteFile(Constants.SAVENAME_SEA_CONTROLLER);
			} catch (IOException e) {
				logger.error("Could not remove existing save files!");
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		} else {
			CliInput cliLoaded = (CliInput) load(Constants.SAVENAME_PARAMS, CliInput.class);
			if (cliLoaded != null && cliLoaded.equals(cliInput)) {

				logger.info("Try to load last run");
				RouteControllerDTO routeControllerDTO = (RouteControllerDTO) load(Constants.SAVENAME_ROUTE_CONTROLLER,
						RouteControllerDTO.class);
				routeController = new RouteController(routeControllerDTO);
				waypointController = (WaypointController) load(Constants.SAVENAME_WAYPOINT_CONTROLLER,
						WaypointController.class);
				seaController = (SeaNodeFactory) load(Constants.SAVENAME_SEA_CONTROLLER, SeaNodeFactory.class);
				logger.info("Loading successfully");
				return true;
			}
		}
		logger.info("Loading failed");
		return false;
	}

	/**
	 * @param fileNamePath
	 * @throws IOException
	 */
	private void deleteFile(String fileNamePath) throws IOException {
		logger.info("Try to delete file: " + fileNamePath);
		logger.info("Remove successful: " + Files.deleteIfExists(Paths.get(fileNamePath)));
	}

	/**
	 * @param savenameRouteController
	 * @param class1
	 * @param class2
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object load(String saveName, Class classname) {
		Gson gson = new Gson();
		try {
			logger.info("  try to load " + saveName + " ...");
			Object object = gson.fromJson(new FileReader(saveName), classname);
			logger.info("  loading finished: " + saveName);
			return object;
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			logger.error("Loading from " + saveName + " failed");
		}
		return null;
	}

	private void initControllers() {
		routeController = new RouteController();
		waypointController = new WaypointController();

	}

	private void writeInputToConstants() {

		if (cliInput.timeStepDelay != 0) {
			Constants.TIME_STEP_DELAY = cliInput.timeStepDelay;
		}
		if (cliInput.paintStepCount != 0) {
			Constants.PAINT_STEPS_COUNT = cliInput.paintStepCount;
		}
		Constants.zoomAggregation = cliInput.zoomAggregation;
		if (Constants.zoomAggregation) {
			if (cliInput.zoomLevel != 0) {
				Constants.ZOOM_LEVEL_COUNT = cliInput.zoomLevel;
			}
		}
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
			mainFrame.updateTimeSlider(currentTime);
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
		mapViewer = new MyMap(this, routeController, waypointController);
		SeaRouteController.setMap(mapViewer);

		mainFrame = new MainFrame(this, mapViewer);
		mainFrame.setResizable(true);
		// mainFrame.setVisible(true);

		routeController.addPropertyChangeListener(mapViewer);
	}

	private JTextArea createLoggerFrame() {

		JTextArea ta = new JTextArea();
		@SuppressWarnings("resource")
		TextAreaOutputStream taos = new TextAreaOutputStream(ta, 60);
		LogManager.getRootLogger().addAppender(taos.getAppender());

		JFrame frame = new JFrame("Logger Output");
		frame.add(new JScrollPane(ta));
		frame.pack();
		frame.setSize(800, 600);
		frame.setVisible(true);
		return ta;
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
				logger.info("Finished edge optimizing");
				logger.info("Try to save current edge state to working directory");
				saveCurrentEdgeState();
				finishedEdgeOptimizing();
			}
		});
		t.start();

	}

	private void finishedEdgeOptimizing() {
		PaintController.getInstance().useNewPainter();
		mapViewer.setWaypoints(new HashSet<>(waypointController.getWaypoints(mapViewer.getZoom())));
		repaint();
		mainFrame.updateProgessBar(100);
		logger.info("_____________________");
		logger.info("| Running Completed |");
		logger.info("̅̅̅̅̅̅̅̅̅̅̅̅̅̅̅̅̅̅̅̅̅");
	}

	/**
	 * Saves the current {@link Edge} state to the working directory. Also saves
	 * a footprint of the input files for recognizing and reloading.
	 */
	private void saveCurrentEdgeState() {
		save(new RouteControllerDTO(routeController), Constants.SAVENAME_ROUTE_CONTROLLER);

		save(waypointController, Constants.SAVENAME_WAYPOINT_CONTROLLER);
		save(seaController, Constants.SAVENAME_SEA_CONTROLLER);
		save(cliInput, Constants.SAVENAME_PARAMS);
		// save(mapViewer, Constants.SAVENAME_MAP);
	}

	private void save(Object object, String name) {
		Gson gson = new Gson();
		try (FileWriter writer = new FileWriter(new File(name))) {
			writer.write(gson.toJson(object));
			logger.info("  saved " + name);
		} catch (IOException e) {
			logger.error("State could not be saved");
			e.printStackTrace();
		}
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
	public SeaNodeFactory getSeaController() {
		return seaController;
	}

	/**
	 * 
	 */
	public void repaint() {
		mapViewer.repaint();
	}

}
