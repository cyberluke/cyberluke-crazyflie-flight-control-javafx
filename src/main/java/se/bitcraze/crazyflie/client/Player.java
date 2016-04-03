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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.Crazyflie;

public class Player extends Thread {

	private static final Logger log = LoggerFactory.getLogger(Player.class);
	private final Crazyflie crazyflie;

	public Player(Crazyflie crazyflie) {
		this.crazyflie = crazyflie;
	}

	@Override
	public void run() {
		File f = new File("flightdata.csv");
		log.info("Read from {}", f.getAbsolutePath());
		try {
			FileReader in = new FileReader(f);
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			while ((line = reader.readLine()) != null) {
				log.info("Read Line: {}", line);
				String[] data = line.split(";");
				crazyflie.sendSetpoint(Float.parseFloat(data[0]),
						Float.parseFloat(data[1]), Float.parseFloat(data[2]),
						(char) Integer.parseInt(data[3]));
				Thread.sleep(10);
			}
			reader.close();
			log.info("Finished from {}", f.getAbsolutePath());
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			return;
		} catch (Exception e) {
			log.error("Failed to Play recorded data", e);
		}

	}

}
