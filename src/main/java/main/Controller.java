package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import gui.Map;
import gui.RunButton;
import models.Edge;
import models.HighResEdge;
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

	private ForkJoinPool pool;
	
	private Map mapViewer;
	private ModelLoader input;
	private GraphHopper graphHopper;
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
		pool = new ForkJoinPool();
	}

	private void optimizeEdgesWithGH() {
		// GHPoint köln = new GHPoint(50.933330, 6.950000);
		// GHPoint frankfurt = new GHPoint(50.115520, 8.684170);
		//
		// GHRequest ghRequest = new GHRequest(köln, frankfurt);
		// GHResponse response = graphHopper.route(ghRequest);
		//
		// PathWrapper path = response.getBest();
		// PointList points = path.getPoints();

		for (Edge edge : mapViewer.getRoute()) {
			pool.invoke(new ForkJoinTask<Edge>() {

				private static final long serialVersionUID = 1475668164109020735L;

				@Override
				public Edge getRawResult() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				protected void setRawResult(Edge value) {
					// TODO Auto-generated method stub
					
				}

				@Override
				protected boolean exec() {
					HighResEdge highResEdge = getHighRes(edge);
					System.out.println("DONE");
					return mapViewer.updateEdge(edge, highResEdge);
				}
			});
			
			
		}

//		mapViewer.importGrapHopperPoints(points);

	}

	private HighResEdge getHighRes(Edge edge) {
		GHPoint start = new GHPoint(edge.getStart().getLatitude(), edge.getStart().getLongitude());
		GHPoint dest = new GHPoint(edge.getDest().getLatitude(), edge.getDest().getLongitude());

		GHRequest ghRequest = new GHRequest(start, dest);
		GHResponse response = graphHopper.route(ghRequest);

		PathWrapper path = response.getBest();
		PointList points = path.getPoints();
		
		HighResEdge highResEdge = new HighResEdge(edge);
		highResEdge.addPositions(points.toGeoJson());
		return highResEdge;
	}

	private void initGraphhopper() {
		graphHopper = new GraphHopperOSM().forDesktop();
		// GraphHopperStorage graph = graphHopper.getGraphHopperStorage();

		CarFlagEncoder encoder = new CarFlagEncoder();
		graphHopper.setEncodingManager(new EncodingManager(encoder));
		graphHopper.getCHFactoryDecorator().setEnabled(false);

		long time = System.currentTimeMillis();
		System.out.println("Start init GH");
		try {
			CmdArgs args = CmdArgs.readFromConfig("src/main/resources/graphhopper/config.properties",
					"graphhopper.config");
			graphHopper.init(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graphHopper.importOrLoad();
		System.out.println("End init GH - " + (System.currentTimeMillis() - time));
	}

	// void graphop() {
	// // import OpenStreetMap data
	// GraphHopper hopper = new GraphHopper();
	// // hopper.setOSMFile("./map-data/leipzig_germany.osm.pbf");
	// hopper.setGraphHopperLocation("./target/mapmatchingtest");
	// CarFlagEncoder encoder = new CarFlagEncoder();
	// hopper.setEncodingManager(new EncodingManager(encoder));
	// hopper.getCHFactoryDecorator().setEnabled(false);
	// hopper.importOrLoad();
	//
	// // create MapMatching object, can and should be shared accross threads
	//
	// FlagEncoder flagEncoder = new CarFlagEncoder();
	// Weighting weighting = new ShortestWeighting(flagEncoder);
	// AlgorithmOptions algoOptions = new
	// AlgorithmOptions(Parameters.Algorithms.DIJKSTRA_BI, weighting);
	// MapMatching mapMatching = new MapMatching(hopper, algoOptions);
	//
	// // do the actual matching, get the GPX entries from a file or via stream
	// List<GPXEntry> inputGPXEntries = new
	// GPXFile().doImport("nice.gpx").getEntries();
	// MatchResult mr = mapMatching.doWork(inputGPXEntries);
	//
	// // return GraphHopper edges with all associated GPX entries
	// List<EdgeMatch> matches = mr.getEdgeMatches();
	// // now do something with the edges like storing the edgeIds or doing
	// // fetchWayGeometry etc
	// matches.get(0).getEdgeState();
	// }

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

		// init GH
		long time = System.currentTimeMillis();
		System.out.println("Start GH");
		initGraphhopper();
		// optimize GH edges
		optimizeEdgesWithGH();
		System.out.println("End GH - " + (System.currentTimeMillis() - time));
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
