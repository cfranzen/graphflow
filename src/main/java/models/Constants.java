package models;

public class Constants {

	// XXX for debug
	public static boolean onlyGermany = false;
	public static boolean debugInfos = false;
	public static boolean debugInfosSeaEdges = false;
	public static boolean drawOnlyViewport = true;
	public static boolean optimzeLandRoutes = true;
	public static boolean showTimesteps = true;
	public static boolean drawLineWhenEnitityStarts = true;
	
	
	public static final int MAX_ZOOM_LEVEL = 17;
	public static final double POINT_DISTANCE_GEO = 2.75;
	public static final int PAINT_STEPS = 50;
	public static final int DEBUG_ZOOM = 6;

	/**
	 * Delay between increasing the time step counter in milliseconds
	 */
//	public static final int TIME_STEP_DELAY = 75;
	public static final int TIME_STEP_DELAY = 30;

	/**
	 * Limits the shown time steps for debug purposes
	 */
	public static final int MAX_TIME_STEPS = 250;
	public static final double SEA_NODE_MAX_DISTANCE = 300000;
	public static final float SHIP_SCALE_FACTOR = 1.95f;
	
	public static final int CIRCLE_DIAMETER = 10;

	public static final String EVENT_NAME_WAYPOINT = "highlightedWaypoint";
	
}
