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
package se.bitcraze.crazyflie.client.controller;

import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Huber
 * 
 */
public abstract class AbstractInputDevice implements InputDevice, Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractInputDevice.class);
	private final Controller controller;
	private final Properties properties;
	private CopyOnWriteArrayList<InputListener> listeners;

	private Thread pollerThread;
	private Component althold;
	private Component killswitch;

	private long readRate;
	private float lastAlthold;
	private float lastKillswitch;
	private boolean on = false;
	private boolean altHold = false;

	/**
	 * 
	 */
	public AbstractInputDevice(Controller controller, Properties properties) {
		this.controller = controller;
		this.properties = properties;
		this.listeners = new CopyOnWriteArrayList<InputListener>();
		readRate = Long.parseLong(properties.getProperty(
				"controller.read.rate", "10"));

		initialize(controller, properties);
	}

	protected void initialize(Controller controller, Properties properties) {
		String ah = properties.getProperty("controller.althold", "5");
		String ks = properties.getProperty("controller.killswitch", "6");


		for (Component c : controller.getComponents()) {
			String id = c.getIdentifier().getName();
			if (ah.equals(id)) {
				althold = c;
				log.info("AltHold: {}, analog {}", c, c.isAnalog());
			}
			if (ks.equals(id)) {
				killswitch = c;
				log.info("Killswith: {}, analog {}", c, c.isAnalog());
			}
		}

	}

	protected Controller getController() {
		return controller;
	}

	protected Properties getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.bitcraze.crazyflie.client.controller.InputDevice#connect()
	 */
	@Override
	public void connect() {
		pollerThread = new Thread(this, "[InputDevice]: PollerThread");
		pollerThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.bitcraze.crazyflie.client.controller.InputDevice#disconnect()
	 */
	@Override
	public void disconnect() {
		pollerThread.interrupt();
		pollerThread = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.bitcraze.crazyflie.client.controller.InputDevice#addListener(se.bitcraze
	 * .crazyflie.client.controller.InputListener, java.lang.String[])
	 */
	@Override
	public void addListener(InputListener listener, String... identifier) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.bitcraze.crazyflie.client.controller.InputDevice#removeListener(se
	 * .bitcraze.crazyflie.client.controller.InputListener)
	 */
	@Override
	public void removeListener(InputListener listener) {
		listeners.remove(listener);
	}

	protected void notifyEvent(InputEvent event) {
		for (InputListener l : listeners)
			l.onInput(event);
	}

	protected void pollData() {
		if (althold != null) {
			float value = althold.getPollData();
			if (althold.isAnalog()) {
				if (value > -0.1f && value < 0.1f) {
					if (on && !altHold) {
						log.info("Altitude Hole On: {}", value);
						notifyEvent(new AltHoldEvent(true));
						altHold = true;

					}
				} else {
					if (altHold) {
						log.info("Altitude Hole Off: {}", value);
						notifyEvent(new AltHoldEvent(false));
						altHold = false;
					}
				}
			} else {
				if (value != lastAlthold) {
					notifyEvent(new AltHoldEvent(value > 0.0f));
					lastAlthold = value;
				}
			}
		}
		if (killswitch != null) {
			float value = killswitch.getPollData();
			if (killswitch.isAnalog()) {
				if (value <= -0.2f) {
					// Switch on when off
					if (!on) {
						log.info("Motors on: {}", value);
						notifyEvent(new StartEvent(true));
						on = true;
					}
				} else if (value >= 0.9f) {
					if (on) {
						log.info("Motors Off {0}", value);
						notifyEvent(new StartEvent(false));
						on = false;
					}
				}
			} else {
				if (value != lastKillswitch) {
					if (value > 0.0f) {
						on = !on;
						log.info("Motors {}", on ? "on" : "off");
						notifyEvent(new StartEvent(on));
					}
					lastKillswitch = value;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(readRate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			if (controller.poll())
				pollData();
		}
	}

}
