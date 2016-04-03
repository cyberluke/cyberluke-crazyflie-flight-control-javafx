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
package se.bitcraze.crazyflie.client.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.LogListener;
import eu.hansolo.steelseries.extras.Battery;

public class MainHeader extends JComponent implements LogListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8348570850854621938L;
	private static final Logger log = LoggerFactory.getLogger(MainHeader.class);
	private DecimalFormat voltageFormat = new DecimalFormat("0.00 V");
	private JLabel voltage;
	private Battery battery;
	private static final float MIN_BAT = 3.15f;
	private static final int CHARGE = 1;
	private static final int FULL = 2;

	public MainHeader(final Crazyflie crazyflie) {
		super();
		crazyflie.addListener(MainHeader.this, "pm");
		setLayout(new FlowLayout());
		voltage = new JLabel();
		voltage.setPreferredSize(new Dimension(50, 10));
		add(voltage);
		battery = new Battery();
		battery.setPreferredSize(new Dimension(50, 10));
		add(battery);
	}

	// public void doLayout() {
	// int width = getWidth();
	// int height = getHeight();
	//
	// Dimension buttonSize = battery.getPreferredSize();
	// int yPos = (height - buttonSize.height) / 2;
	// int xPos = width - buttonSize.width - yPos;
	// battery.setBounds(xPos, yPos, buttonSize.width, buttonSize.height);
	// }
	//
	// public Dimension getPreferredSize() {
	// Dimension size = battery.getPreferredSize();
	// return size;
	// }

	@Override
	public void valuesReceived(String name, Map<String, Object> values) {
		if (values.containsKey("pm.vbat")) {
			float v = (Float) values.get("pm.vbat");
			voltage.setText(voltageFormat.format(v));
			int percent = (int) ((v - MIN_BAT) * 100);
			battery.setValue(percent);
		}
	}
}
