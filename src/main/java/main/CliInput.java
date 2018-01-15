package main;

import org.kohsuke.args4j.Option;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class CliInput{
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
	
	@Option(name = "-logLevel", usage="Shows a separate logger window if one option is given. Options:'ALL', 'DEBUG', 'INFO', 'WARN', 'ERROR'")
	public String logLevel;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ghFolder == null) ? 0 : ghFolder.hashCode());
		result = prime * result + ((logLevel == null) ? 0 : logLevel.hashCode());
		result = prime * result + ((modelFilePath == null) ? 0 : modelFilePath.hashCode());
		result = prime * result + ((osmFilePath == null) ? 0 : osmFilePath.hashCode());
		result = prime * result + paintStepCount;
		result = prime * result + ((seaNodes == null) ? 0 : seaNodes.hashCode());
		result = prime * result + ((solutionFilePath == null) ? 0 : solutionFilePath.hashCode());
		result = prime * result + timeStepDelay;
		result = prime * result + (zoomAggregation ? 1231 : 1237);
		result = prime * result + zoomLevel;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CliInput other = (CliInput) obj;
		if (ghFolder == null) {
			if (other.ghFolder != null)
				return false;
		} else if (!ghFolder.equals(other.ghFolder))
			return false;
		if (logLevel == null) {
			if (other.logLevel != null)
				return false;
		} else if (!logLevel.equals(other.logLevel))
			return false;
		if (modelFilePath == null) {
			if (other.modelFilePath != null)
				return false;
		} else if (!modelFilePath.equals(other.modelFilePath))
			return false;
		if (osmFilePath == null) {
			if (other.osmFilePath != null)
				return false;
		} else if (!osmFilePath.equals(other.osmFilePath))
			return false;
		if (paintStepCount != other.paintStepCount)
			return false;
		if (seaNodes == null) {
			if (other.seaNodes != null)
				return false;
		} else if (!seaNodes.equals(other.seaNodes))
			return false;
		if (solutionFilePath == null) {
			if (other.solutionFilePath != null)
				return false;
		} else if (!solutionFilePath.equals(other.solutionFilePath))
			return false;
		if (timeStepDelay != other.timeStepDelay)
			return false;
		if (zoomAggregation != other.zoomAggregation)
			return false;
		if (zoomLevel != other.zoomLevel)
			return false;
		return true;
	}
	
	
	
}
