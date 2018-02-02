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
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.awtui.ProgressBar;
import main.MainController;
import newVersion.main.PaintController;

/**
 * Mainframe for the whole program.
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 *
 */
public class MainFrame extends JFrame {

	private static final long serialVersionUID = 4522268379182400290L;
	private static final String FRAME_NAME = "Graphstream";
	// private JFrame mainFrame;
	private JLabel timeTextLabel = new JLabel();
	private JLabel lastMessageLabel = new JLabel();
	private String timeText = "Time: 0";
	private JProgressBar progressBar;
	private JSlider timeSlider;

	/**
	 * @param ta
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
				progressBar.setBounds(0, (int) (getSize().getHeight() - 55), getSize().width - 15, 10);
				timeSlider.setBounds(1, (int) (getSize().getHeight() - 97), getSize().width - 15, 43);
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

		JButton painterBtn = new JButton();
		painterBtn.setText("Next Painter");
		painterBtn.setBounds(0, 115, 142, 25);
		painterBtn.addMouseListener(new SimpleMouseClickListener() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					PaintController.getInstance().nextPainter(true);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					PaintController.getInstance().nextPainter(false);
				}
			}
		});
		layeredPane.add(painterBtn, new Integer(40));
		
		JButton screenshotBtn = new JButton();
		screenshotBtn.setText("Make screenshot");
		screenshotBtn.setBounds(0, 145, 142, 25);
		screenshotBtn.addMouseListener(new SimpleMouseClickListener() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				ImageWriter.makeScreenshot(mapViewer);
			}
		});
		layeredPane.add(screenshotBtn, new Integer(40));
		
		
		

		// TextArea for in window display, consumes much ressources
		// JTextArea ta = new JTextArea();
		// LogManager.getRootLogger().addAppender(new TextAreaOutputStream(ta,
		// 3).getAppender());
		// ta.setBounds(5, (int) (getSize().getHeight() - 180), getSize().width,
		// 48);
		// ta.setOpaque(false);
		// layeredPane.add(ta, new Integer(30));

		timeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		timeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int time = (int) source.getValue();
					controller.setTime(time);
				}
			}
		});
		timeSlider.setMajorTickSpacing(100);
		timeSlider.setMaximum(3000);
		timeSlider.setPaintTicks(true);
		timeSlider.setPaintLabels(true);
		timeSlider.setBounds(0, (int) (getSize().getHeight() - 155), getSize().width, 15);
		layeredPane.add(timeSlider, new Integer(30));

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setBounds(0, (int) (getSize().getHeight() - 55), getSize().width, 10);
		progressBar.setMaximum(100);
		layeredPane.add(progressBar, new Integer(30));
	}

	public void updateTimeText(int currentTime) {
		timeText = "Time: " + currentTime;
		timeTextLabel.setText(timeText);
	}

	public void updateLastMessage(String message) {
		lastMessageLabel.setText(message);
	}

	/**
	 * New value of the {@link ProgressBar} in percent
	 * 
	 * @param value
	 */
	public void updateProgessBar(int value) {
		if (value == -1) {
			progressBar.setIndeterminate(true);
		} else if (value >= 0 && value <= 100) {
			progressBar.setValue(value);
			progressBar.setIndeterminate(false);
		}
	}

	public void updateTimeSlider(int value) {
		timeSlider.setValue(value);
	}

	public void maxTimeSlider(int value) {
		timeSlider.setMajorTickSpacing(value / 20);
		timeSlider.setMaximum(value);
	}

}
