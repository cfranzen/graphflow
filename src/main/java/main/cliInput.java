/**
 * 
 */
package main;

import org.kohsuke.args4j.Option;

/**
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class cliInput{
	@Option(name = "-osmFile", usage = "filepath with OSM data, only needed on first initialization")
	public String osmFilePath = "";

	@Option(name = "-modelFile", usage = "file path to the model")
	public String modelFilePath;

	@Option(name = "-solutionFile", usage = "file path to the preprocessed solution")
	public String solutionFilePath;
	
	@Option(name = "-ghFolder", usage = "graphhopper folder with config file")
	public String ghFolder;
	
	@Option(name = "-seaNodes", usage = "file path to the seanodes file")
	public String seaNodes;
}
