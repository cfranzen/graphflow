/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import main.MainController;

/**
 * Mainframe for the whole program.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MainFrame extends JFrame {

	private static final long serialVersionUID = 4522268379182400290L;
	private static final String FRAME_NAME = "Graphstream";
//	private JFrame mainFrame;
	private JLabel timeTextLabel = new JLabel();
	private String timeText = "Time: 0";
	
	/**
	 * 
	 */
	public MainFrame(MainController controller, MyMap mapViewer) {
		// Display the viewer in a JFrame
		setName(FRAME_NAME);
		setSize(1600, 800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		setLayout(new BorderLayout());
		setBackground(Color.YELLOW);
		getContentPane().setBackground(Color.PINK);

		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				mapViewer.setSize(getSize());

			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});

		JLayeredPane layeredPane = new JLayeredPane();
		// layeredPane.setLayout(new SpringLayout());
		layeredPane.setBackground(Color.BLUE);
		add(layeredPane, BorderLayout.CENTER);

		JButton btn = new RunButton(controller);
		btn.setSize(142, 50);
		layeredPane.add(btn, new Integer(20));

		JButton plusBtn = new JButton();
		plusBtn.setText("+");
		plusBtn.setBounds(0, 52, 70, 25);
		plusBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.incTime();
			}
		});
		layeredPane.add(plusBtn, new Integer(30));
		JButton minusBtn = new JButton();
		minusBtn.setText("-");
		minusBtn.setBounds(72, 52, 70, 25);
		minusBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.reduceTime();
			}
		});
		layeredPane.add(minusBtn, new Integer(40));

		// Add time table to frame
		JPanel timePanel = new JPanel();
		timePanel.add(timeTextLabel);
		timeTextLabel.setText(timeText);
		timePanel.setBounds(0, 80, 142, 30);
		layeredPane.add(timePanel, new Integer(30));

		// Add viewer to frame
		layeredPane.add(mapViewer, new Integer(10));
	}

	
	public void updateTimeText(int currentTime) {
		timeText = "Time: " + currentTime;
		timeTextLabel.setText(timeText);
	}
	
}
