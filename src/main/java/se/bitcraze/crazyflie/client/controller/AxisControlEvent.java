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

/**
 * @author Andreas Huber
 * 
 */
public class AxisControlEvent implements InputEvent {

	private final float yaw;
	private final float trust;
	private final float roll;
	private final float pitch;

	/**
	 * 
	 */
	public AxisControlEvent(float yaw, float trust, float pitch, float roll) {
		this.yaw = yaw;
		this.trust = trust;
		this.pitch = pitch;
		this.roll = roll;
	}

	/**
	 * @return the x
	 */
	public float getYaw() {
		return yaw;
	}

	/**
	 * @return the y
	 */
	public float getTrust() {
		return trust;
	}

	/**
	 * @return the rz
	 */
	public float getPitch() {
		return pitch;
	}

	/**
	 * @return the z
	 */
	public float getRoll() {
		return roll;
	}

}
