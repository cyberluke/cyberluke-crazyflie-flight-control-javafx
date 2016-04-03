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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recorder {

	private static final Logger log = LoggerFactory.getLogger(Recorder.class);
	private volatile boolean started;
	private volatile FileWriter out;

	public Recorder() {

	}

	public void start() {
		try {
			File f = new File("flightdata.csv");
			if (!f.exists())
				f.createNewFile();
			out = new FileWriter(f);
			started = true;
			log.info("Recording started");
		} catch (IOException e) {
			log.error("Failed to start recording", e);
		}

	}

	public void stop() {
		if (!started)
			return;
		try {
			if (out != null)
				out.close();
			log.info("Recording stopped");
			started = false;
		} catch (IOException e) {
			log.error("Failed to close file", e);
		}
	}

	public void record(float roll, float pitch, float yaw, char thrust) {
		if (!started)
			return;
		try {
			out.write(Float.toString(roll));
			out.write(";");
			out.write(Float.toString(pitch));
			out.write(";");
			out.write(Float.toString(yaw));
			out.write(";");
			out.write(Integer.toString(thrust));
			out.write("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
