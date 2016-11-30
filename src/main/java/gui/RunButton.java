package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import main.Controller;

/**
 * Starts the running of the graph.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class RunButton extends JButton {

	private static final long serialVersionUID = 7075516178591995755L;
	private static String STOPPED = "Run";
	private static String RUNNING = "Stop";

	/**
	 * 
	 */
	public RunButton(Controller controller) {
		setText(STOPPED);
		addActionListener(new ActionListener() {

			private boolean flag = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				flag = !flag;
				if (!flag) {
					setText(STOPPED);
					return;
				}
				setText(RUNNING);
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						while (flag) {
							controller.incTime();
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});
				t.start();
			}
		});
		setSize(150, 50);
	}

}
