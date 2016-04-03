# Crazyflie Flight Control

This is a Java based GUI to Control the Crazyflie Quadcopter.
Some Configuration Options are available in src/main/resources/FlightControl.properties
The generated jar file is Executable and includes all required libraries.

To Start the Program you need to compile crazyflie-lib and install it in your local
Maven Repository with "mvn clean install".

You are able to start this GUI Application with 

mvn exec:java -Dexec.mainClass="se.bitcraze.crazyflie.client.FlightControl"
or you start the jar file direcly with java -jar crazyflie-client-1.0-SNAPSHOT.jar

Windows 7 User need to install the WinUSB Driver instead of Crazyradio Driver.
This Driver can be downloaded from http://zadig.akeo.ie/






