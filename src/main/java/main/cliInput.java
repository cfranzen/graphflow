package main;

import org.kohsuke.args4j.Option;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class cliInput{
	@Option(name = "-osmFile", usage = "filepath with OSM data, only needed on first initialization")
	public String osmFilePath = "";

	@Option(name = "-modelFile", required=true, usage = "file path to the model")
	public String modelFilePath;

	@Option(name = "-solutionFile", required=true, usage = "file path to the preprocessed solution")
	public String solutionFilePath;
	
	@Option(name = "-ghFolder", required=true, usage = "graphhopper folder with config file")
	public String ghFolder;
	
	@Option(name = "-seaNodes",  usage = "file path to the seanodes file")
	public String seaNodes;
	
	@Option(name = "-zoomLevel", usage = "number of zoom level for summarizing the waypoints")
	public int zoomLevel;
	
	@Option(name = "-timeStepDelay", usage = "number of delay between every frame in ms")
	public int timeStepDelay;
	
	@Option(name = "-paintStepCount", usage = "number of frames which has every time unit")
	public int paintStepCount;

	@Option(name = "-zoomAggregation", usage = "If true, waypoints will be aggregated when zooming out of the map")
	public boolean zoomAggregation;
	
}
