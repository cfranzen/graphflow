package old.examples;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.SortedSet;

import org.apache.commons.io.IOUtils;

import com.syncrotess.pathfinder.Constants;
import com.syncrotess.pathfinder.model.entity.EntityFactory;
import com.syncrotess.pathfinder.model.entity.Model;
import com.syncrotess.pathfinder.model.entity.Node;
import com.syncrotess.pathfinder.model.entity.Service;
import com.syncrotess.pathfinder.model.entity.impl.EntityFactoryImpl;
import com.syncrotess.pathfinder.model.parser.ModelParser;
import com.syncrotess.pathfinder.model.parser.StringTokenModelParser;
import com.syncrotess.pathfinder.model.reader.ModelReader;
import com.syncrotess.pathfinder.model.reader.ReaderUtils;
import com.syncrotess.pathfinder.model.reader.TextFileModelReader;
import com.syncrotess.pathfinder.util.tokenizer.CharacterStreamTokenizer;

public class ExampleModelLoading {
	
	public static boolean printDebug = false;

	public static void main(final String[] args) {
		loadTestFile();
	}

	public static ExampleModelLoading loadTestFile() {
		File modelFile = new File("src/main/resources/examples/testModel.txt.gz");

		Reader fileReader = null;
		Model model = null;
		try {
			fileReader = ReaderUtils.getReaderForFile(modelFile, Constants.DEFAULT_CHARSET);
			final CharacterStreamTokenizer tokenizer = new CharacterStreamTokenizer(fileReader);
			final ModelReader modelReader = new TextFileModelReader(tokenizer);
			final EntityFactory entityFactory = new EntityFactoryImpl();
			final ModelParser modelParser = new StringTokenModelParser(modelReader, entityFactory);
			model = modelParser.parse();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileReader);
		}

		SortedSet<Node> allNodes = model.getServiceNetwork().getNodes();
		SortedSet<Service> allEdges = model.getServiceNetwork().getServices();
		if (printDebug) {
			System.out.println("All nodes of network:");
			for (Node node : allNodes) {
				System.out.println(node.getName() + " [" + node.getLatitude() + ", " + node.getLongitude() + "]");
			}
	
			System.out.println("All edges of network:");
			for (Service edge : allEdges) {
				System.out.println(edge.getStartNode().getName() + "->" + edge.getEndNode().getName() + " - Capacity: "
						+ edge.getCapacities());
			}
		}
		return new ExampleModelLoading(allNodes, allEdges);
	}

	/**
	 * @param nodes
	 * @param edges
	 */
	public ExampleModelLoading(SortedSet<Node> nodes, SortedSet<Service> edges) {
		super();
		this.nodes = nodes;
		this.edges = edges;
	}

	public SortedSet<Node> nodes;
	public SortedSet<Service> edges;

}
