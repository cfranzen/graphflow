package models;

public class Constants {

	// XXX for debug
	public static boolean onlyGermany = false;
	public static boolean debugInfos = true;
	public static boolean debugInfosSeaEdges = true;
	public static boolean drawOnlyViewport = true;
	public static boolean optimzeLandRoutes = false;

	public static final int MAX_ZOOM_LEVEL = 17;
	public static final double POINT_DISTANCE_GEO = 2.75;
	public static final int PAINT_STEPS = 50;
	public static final int DEBUG_ZOOM = 6;

	/**
	 * Delay between increasing the time step counter in milliseconds
	 */
	public static final int TIME_STEP_DELAY = 75; 
	// public static final int TIME_STEP_DELAY = 200;
	/**
	 * Limits the shown time steps for debug purposes
	 */
	public static final int MAX_TIME_STEPS = 250;

}
