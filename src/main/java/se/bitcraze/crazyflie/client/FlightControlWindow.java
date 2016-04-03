/* 
 *  Copyright (C) 2014 Andreas Huber
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package se.bitcraze.crazyflie.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import se.bitcraze.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.client.controller.AxisControlEvent;
import se.bitcraze.crazyflie.client.controller.InputEvent;
import se.bitcraze.crazyflie.client.controller.InputListener;
import se.bitcraze.crazyflie.client.ui.ConnectDialog;
import se.bitcraze.crazyflie.client.ui.ConsoleFooter;
import se.bitcraze.crazyflie.client.ui.FlightDataPanel;
import se.bitcraze.crazyflie.client.ui.LoggingPanel;
import se.bitcraze.crazyflie.client.ui.MainHeader;
import se.bitcraze.crazyflie.client.ui.ParameterPanel;
import se.bitcraze.crazyflie.crtp.CrtpDriver;

/**
 * @author Andreas Huber
 * 
 */
public class FlightControlWindow extends JFrame implements InputListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2548358618544676171L;
	private final Crazyflie crazyflie;
	private java.awt.Container container;
	private JPanel panel;
	private FlightDataPanel flightDataPanel;
	private final Properties properties;
	private JMenuBar menuBar;
	private JDialog connector;
	private Recorder recorder;
	private JCheckBoxMenuItem play;

	/**
	 * 
	 */
	public FlightControlWindow(Crazyflie crazyflie, Properties properties) {
		this.crazyflie = crazyflie;
		this.properties = properties;
		initControls();
		this.setResizable(true);
		this.setVisible(true);

	}

	protected void initControls() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Crazyflie Flight Control");
		container = getContentPane();
		container.setLayout(new BorderLayout());
		menuBar = new JMenuBar();
		JMenu menu = new JMenu("Crazyflie");
		final JMenuItem connect = new JMenuItem("connect");
		connector = new ConnectDialog(this, crazyflie);
		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						connector.setLocation(FlightControlWindow.this
								.getLocation());
						connector.setVisible(true);
					}
				});
			}
		});
		menu.add(connect);
		final JMenuItem disconnect = new JMenuItem("disconnect");
		disconnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					crazyflie.disconnect();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		disconnect.setEnabled(false);
		menu.add(disconnect);
		final JCheckBoxMenuItem record = new JCheckBoxMenuItem("record");
		record.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (record.isSelected())
					recorder.start();
				else
					recorder.stop();

			}
		});
		menu.add(record);
		play = new JCheckBoxMenuItem("play");

		menu.add(play);
		menuBar.add(menu);
		setJMenuBar(menuBar);

		JTabbedPane tabs = new JTabbedPane();

		flightDataPanel = new FlightDataPanel(crazyflie, properties);
		LoggingPanel loggingPanel = new LoggingPanel(crazyflie);
		ParameterPanel parameterPanel = new ParameterPanel(crazyflie);
		tabs.add("Flight Data", flightDataPanel);
		tabs.add("Logging", loggingPanel);
		tabs.add("Parameters", parameterPanel);

		MainHeader header = new MainHeader(crazyflie);
		container.add(header, BorderLayout.NORTH);
		container.add(tabs, BorderLayout.CENTER);
		container.add(new ConsoleFooter(crazyflie), BorderLayout.SOUTH);
		crazyflie.addListener(new ConnectionAdapter() {

			@Override
			public void connectionSetupFinished(CrtpDriver l) {
				super.connectionSetupFinished(l);
				disconnect.setEnabled(true);
				connect.setEnabled(false);
			}

			@Override
			public void disconnected(CrtpDriver l) {
				super.disconnected(l);
				disconnect.setEnabled(false);
				connect.setEnabled(true);
			}
		});

		pack();
	}

	public void setAxis(AxisControlEvent event) {
		// jostickPanel.setAxis(event);
	}

	public boolean isPlay() {
		return play.isSelected();
	}

	public void setRecoder(Recorder recorder) {
		this.recorder = recorder;
	}

	@Override
	public void onInput(InputEvent event) {
		flightDataPanel.onInput(event);
	}

}
