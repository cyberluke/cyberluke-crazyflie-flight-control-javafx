/* 
 *  Copyright (C) 2014 Andreas Huber, Lukas Satin
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
package se.bitcraze.crazyflie.client.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import eu.hansolo.steelseries.tools.KnobType;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.LogListener;
import se.bitcraze.crazyflie.client.controller.AltHoldEvent;
import se.bitcraze.crazyflie.client.controller.AxisControlEvent;
import se.bitcraze.crazyflie.client.controller.InputEvent;
import se.bitcraze.crazyflie.client.controller.InputListener;
import eu.hansolo.steelseries.extras.AirCompass;
import eu.hansolo.steelseries.extras.Altimeter;
import eu.hansolo.steelseries.extras.Horizon;
import se.bitcraze.crazyflie.client.weather.Location;
import se.bitcraze.crazyflie.client.weather.Weather;

/**
 * @author Andreas Huber
 * 
 */
public class FlightDataPanel extends JComponent implements LogListener,
		InputListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2462407047692038120L;

	private Altimeter altimeter;
	private Horizon horizon;
	private AirCompass compass;
	private static final Dimension SIZE = new Dimension(200, 200);
	private final Properties properties;
	private JPanel axisXY;
	private JPanel axisZ;
	private Color xyColor = Color.black;
    private Location myLocation;
    private Weather myWeather;

	/**
	 * 
	 */
	public FlightDataPanel(final Crazyflie crazyflie, Properties properties) {
		this.properties = properties;
		initControls();
		crazyflie.addListener(FlightDataPanel.this, "stabilizer", "baro", "mag");
	}

	private void initControls() {
        // Tell AWT not to bother repainting our canvas since we're
        // going to do that our self in accelerated mode
        setIgnoreRepaint(true);
		// Air Compass
		GridLayout main = new GridLayout(2, 1);
		setLayout(main);
		setMinimumSize(new Dimension(600, 200));
		JPanel instruments = new JPanel();
		instruments.setMinimumSize(new Dimension(600, 200));
		instruments.setLayout(new BoxLayout(instruments, BoxLayout.X_AXIS));
		compass = new AirCompass();
		compass.setSize(SIZE);
		instruments.add(compass);
		// Altimeter
		altimeter = new Altimeter();
		altimeter.setSize(SIZE);
		altimeter.setUnitString("mt");
		altimeter.setLcdUnitString("mt");
		altimeter.setLcdDecimals(2);
        altimeter.setKnobType(KnobType.SMALL_STD_KNOB);
		instruments.add(altimeter);
		// Horizon
		horizon = new Horizon();
		horizon.setSize(SIZE);
		instruments.add(horizon);
		add(instruments);
		JPanel sticks = new JPanel();
		sticks.setSize(new Dimension(200, 100));
		sticks.setMinimumSize(sticks.getSize());
		sticks.setLayout(new BoxLayout(sticks, BoxLayout.X_AXIS));
		// Sticks
		axisXY = new JPanel();
        axisXY.setDoubleBuffered(false);

        axisXY.setSize(new Dimension(100, 100));
		axisXY.setBorder(javax.swing.BorderFactory
				.createLineBorder(new java.awt.Color(0, 0, 0)));

		sticks.add(axisXY);
		axisZ = new JPanel();
        axisZ.setDoubleBuffered(false);

        axisZ.setBorder(javax.swing.BorderFactory
				.createLineBorder(new java.awt.Color(0, 0, 0)));
		axisZ.setSize(new Dimension(100, 100));
		sticks.add(axisZ);
		//add(sticks);
		JPanel data = new JPanel();
		data.setLayout(new GridLayout(1, 2));
        // weather
        try {
            myLocation = new Location();
            myWeather = new Weather(myLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void valuesReceived(String name, Map<String, Object> values) {
		if (values.containsKey("stabilizer.roll")) {
			horizon.setRoll((Float) values.get("stabilizer.roll"));
		}
		if (values.containsKey("stabilizer.pitch")) {
			horizon.setPitch((Float) values.get("stabilizer.pitch"));
		}
		if (values.containsKey("baro.aslLong")) {
			altimeter.setValue((Float) values.get("baro.aslLong"));
		}
        if (values.containsKey("mag.x") && values.containsKey("mag.y") && values.containsKey("mag.z")) {
            float x = (float)values.get("mag.x");
            float y = (float)values.get("mag.y");
            float z = (float)values.get("mag.z");
            // calculate tilt-compensated heading angle
            double cosRoll =  Math.cos(Math.toRadians(horizon.getRoll()));
            double sinRoll =  Math.sin(Math.toRadians(horizon.getRoll()));
            double cosPitch =  Math.cos(Math.toRadians(horizon.getPitch()));
            double sinPitch =  Math.sin(Math.toRadians(horizon.getPitch()));
            double Xh = x * cosPitch + z * sinPitch;
            double Yh = x * sinRoll * sinPitch + y * cosRoll - z * sinRoll * cosPitch;
            double heading =  Math.atan2(Yh, Xh);
            double d_heading = Math.toDegrees(heading);
            System.out.println(d_heading);
            compass.setValue(d_heading);
        }
	}

	@Override
	public void onInput(InputEvent event) {
		if (event instanceof AxisControlEvent) {
			AxisControlEvent e = (AxisControlEvent) event;
			setYawAndTrust(e.getYaw(), e.getTrust());
			setRollAndPitch(e.getRoll(), e.getPitch());
			return;
		}
		if (event instanceof AltHoldEvent) {
			AltHoldEvent e = (AltHoldEvent) event;
			xyColor = e.isHold() ? Color.RED : Color.BLACK;
			return;
		}
	}

	protected void setYawAndTrust(float yaw, float trust) {
		if (!isVisible())
			return;
		Graphics2D g2d = (Graphics2D) axisXY.getGraphics();
		g2d.clearRect(1, 1, axisXY.getWidth() - 2, axisXY.getHeight() - 2);
		g2d.setColor(xyColor);
		int cx = axisXY.getWidth() / 2 - 5;
		int cy = axisXY.getHeight() / 2 - 5;
		int x = (int) (cx + (yaw * cx));
		int y = (int) (cy + (trust * cy));
		g2d.fillOval(x, y, 10, 10);
	}

	protected void setRollAndPitch(float roll, float pitch) {
		if (!isVisible())
			return;
		Graphics2D g2d = (Graphics2D) axisZ.getGraphics();
		g2d.clearRect(1, 1, axisZ.getWidth() - 2, axisZ.getHeight() - 2);
		int crz = axisZ.getWidth() / 2 - 5;
		int cz = axisZ.getHeight() / 2 - 5;
		int rz = (int) (crz + (roll * crz));
		int z = (int) (cz + (pitch * cz));
		g2d.fillOval(rz, z, 10, 10);
	}
}
