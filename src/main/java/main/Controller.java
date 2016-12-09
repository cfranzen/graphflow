package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.matching.GPXFile;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import gui.Map;
import gui.RunButton;
import models.ModelLoader;

/**
 * Main class from the program
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class Controller {

	public static void main(String[] args) {
		Controller controller = getInstance();
		controller.run();
	}

	private static Controller controller;

	private Map mapViewer;
	private ModelLoader input;
	private int currentTime = 0;

	private JFrame frame;

	/**
	 * Returns the {@link Controller} instance, if the instance is
	 * <code>null</code> a new instance is created.
	 * 
	 * @return the {@link Controller} instance
	 */
	public static Controller getInstance() {
		if (controller == null) {
			controller = new Controller();
		}
		return controller;
	}

	/**
	 * Private constructor to override the default and ensure the singleton
	 * pattern.
	 */
	private Controller() {
		// NOP

		// TEST
		GraphHopper graphHopper = new GraphHopperOSM().forDesktop();
//		GraphHopperStorage graph = graphHopper.getGraphHopperStorage();

		CarFlagEncoder encoder = new CarFlagEncoder();
		graphHopper.setEncodingManager(new EncodingManager(encoder));
		graphHopper.getCHFactoryDecorator().setEnabled(false);
		
		try {
			CmdArgs args= CmdArgs.readFromConfig("src/main/resources/graphhopper/config.properties", "graphhopper.config");
			graphHopper.init(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		graphHopper.importOrLoad();
		
		GHPoint köln = new GHPoint(50.933330, 6.950000);
		GHPoint frankfurt = new GHPoint(50.115520, 8.684170);

		GHRequest ghRequest = new GHRequest(köln, frankfurt).setVehicle("car");
		GHResponse response = graphHopper.route(ghRequest);
		
		PathWrapper path = response.getBest();
		PointList points = path.getPoints();
		for (int i = 0; i < points.size(); i++) {
			System.out.println(String.format("ID: %d, LON: %f, LAT: %f", i, points.getLongitude(i), points.getLatitude(i)));
		}
		// TEST

	}

//	void graphop() {
//		// import OpenStreetMap data
//		GraphHopper hopper = new GraphHopper();
//		// hopper.setOSMFile("./map-data/leipzig_germany.osm.pbf");
//		hopper.setGraphHopperLocation("./target/mapmatchingtest");
//		CarFlagEncoder encoder = new CarFlagEncoder();
//		hopper.setEncodingManager(new EncodingManager(encoder));
//		hopper.getCHFactoryDecorator().setEnabled(false);
//		hopper.importOrLoad();
//
//		// create MapMatching object, can and should be shared accross threads
//
//		FlagEncoder flagEncoder = new CarFlagEncoder();
//		Weighting weighting = new ShortestWeighting(flagEncoder);
//		AlgorithmOptions algoOptions = new AlgorithmOptions(Parameters.Algorithms.DIJKSTRA_BI, weighting);
//		MapMatching mapMatching = new MapMatching(hopper, algoOptions);
//
//		// do the actual matching, get the GPX entries from a file or via stream
//		List<GPXEntry> inputGPXEntries = new GPXFile().doImport("nice.gpx").getEntries();
//		MatchResult mr = mapMatching.doWork(inputGPXEntries);
//
//		// return GraphHopper edges with all associated GPX entries
//		List<EdgeMatch> matches = mr.getEdgeMatches();
//		// now do something with the edges like storing the edgeIds or doing
//		// fetchWayGeometry etc
//		matches.get(0).getEdgeState();
//	}

	/**
	 * Main method.
	 * 
	 * TODO distinct gui creation
	 */
	public void run() {

		// Display the viewer in a JFrame
		frame = new JFrame("Graphstream");
		frame.setSize(1600, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setBackground(Color.YELLOW);
		frame.getContentPane().setBackground(Color.RED);

		JLayeredPane layeredPane = new JLayeredPane();
		// layeredPane.setLayout(new BorderLayout() );

		layeredPane.setBackground(Color.BLUE);
		frame.getContentPane().add(layeredPane, BorderLayout.CENTER);

		System.out.println(frame.getSize().toString());
		// layeredPane.setSize(frame.getSize());
		System.out.println(layeredPane.getSize().toString());

		JButton btn = new RunButton(this);
		btn.setSize(150, 50);
		layeredPane.add(btn, new Integer(20));

		// Processing input to own classes
		input = ModelLoader.loadFile();

		// Add input to viewer
		mapViewer = new Map(this);
		mapViewer.addPositions(input.nodes);
		mapViewer.addEdges(input.edges);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(mapViewer, BorderLayout.CENTER);
		mapViewer.setSize(frame.getSize());
		p.setSize(frame.getSize());
		p.setBackground(Color.RED);

		layeredPane.add(p, new Integer(10));

		// TODO resize components on frame resize
		frame.setResizable(false);
		frame.setVisible(true);
	}

	/**
	 * Increases the current time step each time its called. If the maximum time
	 * step from the model is reached it resets the step to zero.
	 */
	public void incTime() {
		currentTime++;
		if (currentTime >= input.timesteps) {
			currentTime = 0;
		}
		mapViewer.setTime(currentTime);
	}

	/**
	 * Returns the map component
	 * 
	 * @return the mapViewer
	 */
	public Map getMapViewer() {
		return mapViewer;
	}
}
