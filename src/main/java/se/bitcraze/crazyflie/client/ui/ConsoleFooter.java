package se.bitcraze.crazyflie.client.ui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.bitcraze.crazyflie.ConsoleListener;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.crtp.CrtpDriver;

public class ConsoleFooter extends JComponent implements ConsoleListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4869872073656691253L;
	private JTextArea console;

	public ConsoleFooter(final Crazyflie crazyflie) {
		super();
		setLayout(new GridLayout(1, 1));

		console = new JTextArea();
		console.setRows(7);
		console.setBackground(Color.WHITE);
		console.setAutoscrolls(true);
		console.setEditable(false);
		JScrollPane scollPane = new JScrollPane(console);
		add(scollPane);
		crazyflie.addListener(this);
	}

	@Override
	public void messageReceived(CrtpDriver driver, String message) {
		console.append(message);
		console.append("\n");
		console.setCaretPosition(console.getText().length());
	}
}
