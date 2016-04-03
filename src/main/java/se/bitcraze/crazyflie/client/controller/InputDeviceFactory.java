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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javafx.concurrent.Task;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new InputDevice
 * 
 * @author Andreas Huber
 * 
 */
public final class InputDeviceFactory {

	private static final Logger log = LoggerFactory
			.getLogger(InputDeviceFactory.class);
	private static final String[] libs = new String[] { "libjinput-osx.jnilib",
			"jinput-dx8_64.dll", "jinput-dx8.dll", "jinput-raw_64.dll",
			"jinput-raw.dll", "jinput-wintab.dll", "libjinput-linux.so",
			"libjinput-linux64.so" };

	/**
	 * 
	 */
	private InputDeviceFactory() {
	}

	public static final InputDevice createInputDevice(Properties properties) {
        extractJInputLibs();
		Controller[] controllersList = ControllerEnvironment
				.getDefaultEnvironment().getControllers();
		// Keyboard Controller will be the fallback solution
		Controller keyboard = null;
		List<Controller> possibleControllers = new ArrayList<Controller>();
		for (Controller controller : controllersList) {
			Type type = controller.getType();
			if (type == Type.KEYBOARD) {
				keyboard = controller;
				continue;
			}
			if (type == Type.GAMEPAD || type == Type.STICK) {
				// TODO check if we have all four axis
                if (controller.getName().contains("Logitech") || controller.getName().contains("USB Controller")) {
				    return new GamepadInputDevice(controller, properties);
                }
			}

		}
		return null;
	}

	public static final void extractJInputLibs() {
		try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
			File folder = new File(System.getProperty("java.io.tmpdir"), "lib");
			log.info("Extract Libs to {}", folder.getAbsolutePath());
			System.setProperty("net.java.games.input.librarypath",
					folder.getAbsolutePath());
			if (!folder.exists())
				folder.mkdirs();

            FileUtils.cleanDirectory(folder);

			for (String lib : libs) {
				try (InputStream in = loader.getResourceAsStream(lib)) {
                    if (in != null) {
				        FileUtils.copyInputStreamToFile(in, new File(folder, lib));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
