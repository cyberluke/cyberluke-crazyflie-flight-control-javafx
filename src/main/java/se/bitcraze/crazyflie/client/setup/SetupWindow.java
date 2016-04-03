package se.bitcraze.crazyflie.client.setup;

import javax.swing.JFrame;

public class SetupWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7142535892122704710L;

	public SetupWindow() {
		this.setResizable(true);
		this.setVisible(true);
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Crazyflie Flight Control Setup");
	}

}
