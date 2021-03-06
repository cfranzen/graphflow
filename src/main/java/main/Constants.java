package main;

import org.apache.log4j.Level;

public class Constants {

	public static int timesteps = 0;
	
	public static boolean onlyGermany = false;
	public static boolean debugInfos = false;
	public static boolean debugInfosSeaEdges = false;
	public static boolean drawOnlyViewport = false;
	public static boolean optimzeLandRoutes = true;
	public static boolean showTimesteps = true;
	public static boolean drawLineWhenEnitityStarts = true;
	public static boolean zoomAggregation = false;

	/**
	 * Anzahl der Zoom-Stufen auf denen aggregiert wird.
	 */
	public static int ZOOM_LEVEL_COUNT = 1;
	public static float ZOOM_LEVEL_DISTANCE_GEO = 6;
	public static float POINT_DISTANCE_GEO = 2.75f;
	/**
	 * Number of frames per time step, defines also
	 */
	public static int PAINT_STEPS_COUNT = 50;
	/**
	 * Delay between increasing the time step counter in milliseconds
	 */
	// public static final int TIME_STEP_DELAY = 75;
	public static int TIME_STEP_DELAY = 30; // Faster for debug
	public static String LOGGER_CONVERSION_PATTERN = "%d{HH:mm:ss,SSS} %-5p [%c{1}] - %m%n";
	public static Level LOGGER_LEVEL = Level.OFF;

	/**
	 * Limits the shown time steps for debug purposes
	 */
	public static final int MAX_TIME_STEPS = 250;
	public static final int MAX_ZOOM_LEVEL = 17;
	public static final int DEBUG_ZOOM = 6;
	public static final double SEA_NODE_MAX_DISTANCE = 300000;
	public static final float SHIP_SCALE_FACTOR = 1.95f;
	public static final int CIRCLE_DIAMETER = 10;
	public static final String SAVENAME_PARAMS = "params.json";
	public static final String SAVENAME_ROUTE_CONTROLLER = "routeControllerSave.json";
	public static final String SAVENAME_WAYPOINT_CONTROLLER = "waypointControllerSave.json";
	public static final String SAVENAME_SEA_CONTROLLER = "seaControllerSave.json";
	public static final String SAVENAME_MAP = "mapSave.json";

	public static final String EVENT_NAME_WAYPOINT_FROM = "highlightedWaypointFrom";
	public static final String EVENT_NAME_WAYPOINT_TO = "highlightedWaypointTo";
	public static final String EVENT_NAME_EDGE_CHANGE = "edgeChanged";
	public static final boolean OPTIMIZE = true;

}
