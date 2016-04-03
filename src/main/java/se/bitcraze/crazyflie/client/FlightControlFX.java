/* 
 *  Copyright (C) 2014 Lukas Satin
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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.bitcraze.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.LogListener;
import se.bitcraze.crazyflie.client.controller.*;
import se.bitcraze.crazyflie.client.setup.SetupWindow;
import se.bitcraze.crazyflie.client.ui.FlightDataPanelFX;
import se.bitcraze.crazyflie.crtp.Crtp;
import se.bitcraze.crazyflie.crtp.CrtpDriver;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Lukas Satin
 * 
 */
public class FlightControlFX  extends Application implements LogListener, InputListener {

	private static final Logger log = LoggerFactory
			.getLogger(FlightControlFX.class);
	private InputDevice inputDevice;
	private FlightControlWindowFX window;
    private Scene defaultScene;
	private Recorder recorder;
	private static Crazyflie crazyflie;
	private float maxRoll = 30.0f;
	private float maxPitch = 30.0f;
	private float maxYaw = 200;
	private volatile boolean on = false;
	private volatile Player player;

	/**
	 *
	 */
	public FlightControlFX() {
	}

    @Override
    public void valuesReceived(String name, Map<String, Object> values) {
        if (window != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    window.getDefaultDataPanel().valuesReceived(name, values);
                }
            });
        }
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
                    release();
				}
			});
			return;
		}

		p.load(getClass().getClassLoader().getResourceAsStream(
				"FlightControl.properties"));
		log.info("Load Properties: {}", p);
		crazyflie = new Crazyflie();
        crazyflie.addListener(FlightControlFX.this, "stabilizer", "baro", "mag", "acc");

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
		/*window = new FlightControlWindow(crazyflie, p);
		window.setRecoder(recorder);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				release();
			}

		});*/
		maxRoll = Float.parseFloat(p.getProperty("crazyflie.max.roll", "30")); //30
		maxPitch = Float.parseFloat(p.getProperty("crazyflie.max.pitch", "30"));
		maxYaw = Float.parseFloat(p.getProperty("crazyflie.max.yaw", "200")); //200

		inputDevice = InputDeviceFactory.createInputDevice(p);
		if (log.isInfoEnabled())
			log.info("Found InputDevice " + inputDevice);
		if (inputDevice != null) {
			inputDevice.addListener(this);
			//inputDevice.addListener(window);
			inputDevice.connect();
		}

        try {
            crazyflie.connect("radio://10/250K");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
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

        if (inputDevice != null) {
            inputDevice.disconnect();
            inputDevice.removeListener(this);
            inputDevice = null;
        }

		try {
			crazyflie.disconnect();
		} catch (Exception e) {
			log.error("Disconnect failed", e);
		}
	}

    @Override
    public void start(Stage stage) throws Exception {
        window = new FlightControlWindowFX();
        defaultScene = window.getDefaultScene();
        stage.setTitle("NANOTRIK.cz - Nano Kvadrokoptéra Multiplatformní Klient");
        stage.setScene(defaultScene);
        stage.setFullScreenExitHint("Stiskněte ESC pro vypnutí režimu celé obrazovky.");
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        stage.setFullScreen(true);
        stage.show();
        FlightControlFX flightControl = new FlightControlFX();
        flightControl.init();
        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    @Override public void stop() {
        release();
    }

    public static void main(String[] args) {
        launch(args);
        /*
        FlightControlFX flightControl = new FlightControlFX();
		flightControl.init();
        */
    }
}
