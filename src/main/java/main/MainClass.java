package main;

/**
 * Entry point of the programm
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MainClass {

	/**
	 * Main method.</br>
	 * Start program with
	 * <ul>
	 * <li>"java -Dsun.java2d.opengl=True" on linux</li>
	 * <li>"java -Dsun.java2d.directx=True" on windows</li>
	 * </ul>
	 * for better performance.
	 * 
	 * @param args
	 *            - default, no arguments needed
	 */
	public static void main(String[] args) {
		new MainController();
		
	
	}

}
