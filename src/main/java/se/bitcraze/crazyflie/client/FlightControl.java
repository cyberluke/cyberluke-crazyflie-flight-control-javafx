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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.client.controller.AltHoldEvent;
import se.bitcraze.crazyflie.client.controller.AxisControlEvent;
import se.bitcraze.crazyflie.client.controller.InputDevice;
import se.bitcraze.crazyflie.client.controller.InputDeviceFactory;
import se.bitcraze.crazyflie.client.controller.InputEvent;
import se.bitcraze.crazyflie.client.controller.InputListener;
import se.bitcraze.crazyflie.client.controller.StartEvent;
import se.bitcraze.crazyflie.client.setup.SetupWindow;
import se.bitcraze.crazyflie.crtp.Crtp;
import se.bitcraze.crazyflie.crtp.CrtpDriver;

/**
 * @author Andreas Huber
 * 
 */
public class FlightControl implements InputListener {

	private static final Logger log = LoggerFactory
			.getLogger(FlightControl.class);
	private InputDevice inputDevice;
	private FlightControlWindow window;
	private Recorder recorder;
	private Crazyflie crazyflie;
	private float maxRoll = 30.0f;
	private float maxPitch = 30.0f;
	private float maxYaw = 200;
	private volatile boolean on = false;
	private volatile Player player;

	/**
	 * 
	 */
	public FlightControl() {
	}

	public void init() throws Exception {
		Properties p = new Properties();
		InputStream in = getClass().getClassLoader().getResourceAsStream(
				"FlightControl.properties");
		if (in == null) {
			log.info("Start Setup");
			new SetupWindow().addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
				}
			});
			return;
		}

		p.load(getClass().getClassLoader().getResourceAsStream(
				"FlightControl.properties"));
		log.info("Load Properties: {}", p);
		crazyflie = new Crazyflie();
		crazyflie.addListener(new ConnectionAdapter() {

			@Override
			public void connectionSetupFinished(CrtpDriver driver) {
				super.connectionSetupFinished(driver);
				crazyflie.getLogging().startAll();
			}

			@Override
			public void disconnected(CrtpDriver driver) {
				super.disconnected(driver);
				on = false;
				recorder.stop();
			}

		});
		// Add All Logging Groups
		crazyflie.getLogging().addGroup("stabilizer", 200, "stabilizer.roll",
				"stabilizer.pitch", "stabilizer.yaw", "stabilizer.thrust");
		crazyflie.getLogging().addGroup("baro", 200, "baro.aslLong",
				"baro.temp", "baro.pressure");
		crazyflie.getLogging().addGroup("gyro", 200, "gyro.x", "gyro.y",
				"gyro.z");
        crazyflie.getLogging().addGroup("mag", 200, "mag.x", "mag.y",
                "mag.z");
		crazyflie.getLogging().addGroup("acc", 200, "acc.x", "acc.y", "acc.z",
				"acc.zw", "acc.mag2");
		crazyflie.getLogging().addGroup("pm", 1000, "pm.vbat", "pm.state");
		// FreeFallDetection freeFall = new FreeFallDetection(crazyflie);
		// crazyflie.addLoggingListener(freeFall, "acc");
		recorder = new Recorder();
		window = new FlightControlWindow(crazyflie, p);
		window.setRecoder(recorder);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				release();
			}

		});
		maxRoll = Float.parseFloat(p.getProperty("crazyflie.max.roll", "30")); //30
		maxPitch = Float.parseFloat(p.getProperty("crazyflie.max.pitch", "30"));
		maxYaw = Float.parseFloat(p.getProperty("crazyflie.max.yaw", "200")); //200

		inputDevice = InputDeviceFactory.createInputDevice(p);
		if (log.isInfoEnabled())
			log.info("Found InputDevice " + inputDevice);
		if (inputDevice != null) {
			inputDevice.addListener(this);
			inputDevice.addListener(window);
			inputDevice.connect();
		}
		// window.setAxis(new AxisControlEvent(0, 0, 0, 0));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.bitcraze.crazyflie.client.controller.InputListener#onInput(se.bitcraze
	 * .crazyflie.client.controller.InputEvent)
	 */
	@Override
	public void onInput(InputEvent event) {
		if (event instanceof AxisControlEvent) {
			AxisControlEvent e = (AxisControlEvent) event;
			if (crazyflie != null && window != null) {
				char thrust = 0;
				float t = e.getTrust() - 1.0f;
                // 650000
				thrust = (char) (Math.abs(t) * (Crtp.MAX_THRUST/2));
				if (on && crazyflie.isConnected() && !window.isPlay()) {
					crazyflie.sendSetpoint(e.getRoll() * maxRoll, e.getPitch()
							* maxPitch, e.getYaw() * maxYaw, thrust);
					recorder.record(e.getRoll() * maxRoll, e.getPitch()
							* maxPitch, e.getYaw() * maxYaw, thrust);
				}
			}
			return;
		}
		if (event instanceof AltHoldEvent) {
			AltHoldEvent e = (AltHoldEvent) event;
			log.info("AltHold: " + e.isHold());
			crazyflie.setAltHold(e.isHold());
			return;
		}

		if (event instanceof StartEvent) {
			on = ((StartEvent) event).isOn();
			if (on && window.isPlay()) {
				player = new Player(crazyflie);
				player.start();
			}
			if (!on) {
				crazyflie.sendSetpoint(0, 0, 0, (char) 10000);
				recorder.stop();
				if (player != null) {
					player.interrupt();
					player = null;
				}

			}

		}

	}

	public void release() {
		if (log.isInfoEnabled())
			log.info("Release FlightControl");
		try {
			// recoder.stop();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			crazyflie.disconnect();
		} catch (Exception e) {
			log.error("Disconnect failed", e);
		}
		if (inputDevice != null) {
			inputDevice.disconnect();
			inputDevice.removeListener(this);
		}
	}

	public static void main(String[] args) throws Exception {
		FlightControl flightControl = new FlightControl();
		flightControl.init();

	}
}
