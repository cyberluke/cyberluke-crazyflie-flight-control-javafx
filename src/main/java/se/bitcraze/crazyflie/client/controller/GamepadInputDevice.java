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

import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Huber
 * 
 */
public class GamepadInputDevice extends AbstractInputDevice {
    private static final Logger log = LoggerFactory.getLogger(GamepadInputDevice.class);

	private Component yawAxis;
	private Component thrustAxis;
	private Component pitchAxis;
	private Component rollAxis;
    private Component turnLeft;
    private Component turnRight;
    private Boolean rollInvert;
    private Boolean pitchInvert;
    private Boolean yawInvert;
    private Boolean thrustInvert;

	/**
	 * @param controller
	 * @param properties
	 */
	public GamepadInputDevice(Controller controller, Properties properties) {
		super(controller, properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.bitcraze.crazyflie.client.controller.AbstractInputDevice#initialize
	 * (net.java.games.input.Controller, java.util.Properties)
	 */
	@Override
	protected void initialize(Controller controller, Properties properties) {
		super.initialize(controller, properties);

		String roll = properties.getProperty("controller.gamepad.roll", "z");
        rollInvert = true;
        String pitch = properties.getProperty("controller.gamepad.pitch", "rz");
        pitchInvert = true;
		String yaw = properties.getProperty("controller.gamepad.yaw", "x");
        yawInvert = true;
		String thrust = properties.getProperty("controller.gamepad.thrust", "y");
        thrustInvert = false;
        String left = properties.getProperty("controller.stick.turnLeft", "6");
        String right = properties.getProperty("controller.stick.turnRight", "7");

		for (Component c : controller.getComponents()) {
			String id = c.getIdentifier().getName();
            System.out.println(id);
            System.out.println(c.getName());
            if (roll.equals(id)) {
				rollAxis = c;
			} else if (pitch.equals(id)) {
				pitchAxis = c;
			} else if (yaw.equals(id)) {
				yawAxis = c;
			} else if (thrust.equals(id)) {
				thrustAxis = c;
			}
            if (left.equals(id)) {
                turnLeft = c;
                log.info("Roll left: {}, analog {}", c, c.isAnalog());
            }
            if (right.equals(id)) {
                turnRight = c;
                log.info("Roll right: {}, analog {}", c, c.isAnalog());
            }
		}
        log.info(pitch);
        //log.info(rollAxis.toString());
        log.info(pitchAxis.toString());
       //log.info(yawAxis.toString());
       // log.info(thrustAxis.toString());
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.bitcraze.crazyflie.client.controller.AbstractInputDevice#pollData()
	 */
	@Override
	protected void pollData() {
		super.pollData();

        float yawAxisData = 0f;

        if (turnRight != null) {
            float valueLeft = 0;
            float valueRight = 0;
            if (turnLeft.isAnalog()) {
                // not supported for now
            } else {
                valueLeft = turnLeft.getPollData();
            }
            if (turnRight.isAnalog()) {
                // not supported for now
            } else {
                valueRight = turnRight.getPollData();
            }
            if (valueLeft == 1.0f) {
                yawAxisData = 1.0f;
            }
            if (valueRight == 1.0f) {
                yawAxisData = -1.0f;
            }
        }

		notifyEvent(new AxisControlEvent(
                yawInvert ? -yawAxisData: yawAxisData,
                thrustInvert ? -thrustAxis.getPollData() : thrustAxis.getPollData(),
                pitchInvert ? -pitchAxis.getPollData() : pitchAxis.getPollData(),
                rollInvert ? -rollAxis.getPollData() : rollAxis.getPollData()));

	}
}
