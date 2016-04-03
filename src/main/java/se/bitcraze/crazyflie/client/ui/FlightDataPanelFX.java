package se.bitcraze.crazyflie.client.ui;

import eu.hansolo.airseries.AirCompass;
import eu.hansolo.airseries.Altimeter;
import eu.hansolo.airseries.Horizon;
import eu.hansolo.enzo.gauge.Gauge;
import eu.hansolo.enzo.gauge.GaugeBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.tbee.javafx.scene.layout.MigPane;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.LogListener;
import se.bitcraze.crazyflie.client.controller.InputEvent;
import se.bitcraze.crazyflie.client.controller.InputListener;
import se.bitcraze.crazyflie.client.fxui.KVPTable;
import se.bitcraze.crazyflie.client.weather.Location;
import se.bitcraze.crazyflie.client.weather.Weather;
import se.bitcraze.crazyflie.client.weather.WeatherService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Lukas Satin
 */
public class FlightDataPanelFX implements LogListener, InputListener {
    private AirCompass compass;
    private Horizon horizon;
    private Altimeter altimeter;
    private Gauge verticalspeedmeter;
    private MigPane layout;
    private Location myLocation;
    private Weather myWeather;

    private Button newBtn;
    private Button prevBtn;
    private Button nextBtn;
    private TextArea textArea;

    private String defaultCity = "Ostrava";

    public Gauge CreateRadialGauge() {
        verticalspeedmeter = GaugeBuilder.create().angleRange(90.0f).autoScale(true).unit("").plainValue(true).title("VERTICAL SPEED").build();
        return verticalspeedmeter;
    }

    public FlightDataPanelFX() {

        compass = new AirCompass();
        horizon = new Horizon();
        altimeter = new Altimeter();
        compass.setAnimated(true);
        horizon.setAnimated(false);
        altimeter.setAnimated(false);

        /*pane = new HBox(compass, horizon, altimeter);
        pane.setPadding(new Insets(20, 20, 20, 20));
        pane.setSpacing(20);
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(31, 31, 31), CornerRadii.EMPTY, Insets.EMPTY)));*/
        newBtn = new Button("Nahrát záznam");
        prevBtn = new Button("<<<<");
        nextBtn = new Button(">>>>");

        /*layout = new MigPane(
                "",                         // Layout Constraints
                "[grow]10[shrink 0]4[shrink 0]",  // Column constraints
                "[][200,grow]");            // Row constraint
        */
        layout = new MigPane();

        layout.add(newBtn);
        layout.add(prevBtn);
        layout.add(nextBtn, "wrap");

        layout.add(compass, "grow, push");

        layout.add(horizon, "grow, push");
        layout.add(altimeter, "grow, push, wrap");

        // weather
        Runnable barrier1Action = new Runnable() {
            public void run() {
                for (int i=0; i<5; i++) {
                    try {
                        myLocation = new Location();
                        System.out.println("Got GeoIP location.");
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Runnable barrier2Action = new Runnable() {
            public void run() {
                for (int i=0; i<5; i++) {
                    try {
                        myWeather = new Weather();
                        if (myLocation == null) {
                            myWeather.getByCity(defaultCity);
                            System.out.println(String.format("Got Weather data for default city %s.", defaultCity));
                            myWeather.getForecastByCity(defaultCity);
                            System.out.println(String.format("Got Forecast data for default city %s.", defaultCity));
                        } else {
                            myWeather.getByLocation(myLocation);
                            System.out.println("Got Weather data by GeoIP.");
                            myWeather.getForecastByLocation(myLocation);
                            System.out.println("Got Forecast data by GeoIP.");
                        }
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Runnable barrier3Action = new Runnable() {
            public void run() {
                System.out.println("Updating UI.");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        ObservableList<Pair<String, Object>> data = FXCollections.observableArrayList(
                                new Pair<String, Object>("Město", myWeather.getCityName()),
                                new Pair<String, Object>("Teplota", myWeather.getTemperatureActual() + " °C"),
                                new Pair<String, Object>("Vítr", myWeather.getWindSpeedTxt()),
                                new Pair<String, Object>("Tlak", myWeather.getPressureTxt()),
                                new Pair<String, Object>("Vlhkost", myWeather.getHumidityTxt()),
                                new Pair<String, Object>("Oblačnost", myWeather.getCloudsInfo()),
                                new Pair<String, Object>("", new Image(myWeather.getCloudsImagePath()))
                        );
                        layout.add(new KVPTable(data).getView());

                        ObservableList<Pair<String, Object>> forecastData = FXCollections.observableArrayList(
                                new Pair<String, Object>(myWeather.getForecastHour(0), new Image(myWeather.getForecastImagePath(0))),
                                new Pair<String, Object>(myWeather.getForecastHour(1), new Image(myWeather.getForecastImagePath(1))),
                                new Pair<String, Object>(myWeather.getForecastHour(2), new Image(myWeather.getForecastImagePath(2))),
                                new Pair<String, Object>(myWeather.getForecastHour(3), new Image(myWeather.getForecastImagePath(3))),
                                new Pair<String, Object>(myWeather.getForecastHour(4), new Image(myWeather.getForecastImagePath(4))),
                                new Pair<String, Object>(myWeather.getForecastHour(5), new Image(myWeather.getForecastImagePath(5))),
                                new Pair<String, Object>(myWeather.getForecastHour(6), new Image(myWeather.getForecastImagePath(6))),
                                new Pair<String, Object>(myWeather.getForecastHour(7), new Image(myWeather.getForecastImagePath(7)))
                        );
                        layout.add(new KVPTable(forecastData).getView());

                        /*layout.add(
                                ImageViewBuilder.create()
                                        .image(new Image(myWeather.getCloudsImagePath()))
                                        .build());*/

                        layout.add(CreateRadialGauge(), "grow, push");

                    }
                });
            }
        };

        CyclicBarrier barrier1 = new CyclicBarrier(1, barrier1Action);
        CyclicBarrier barrier2 = new CyclicBarrier(1, barrier2Action);
        CyclicBarrier barrier3 = new CyclicBarrier(1, barrier3Action);

        WeatherService barrierRunnable1 =
                new WeatherService(barrier1, barrier2, barrier3);

        Thread th = new Thread(barrierRunnable1);
        th.setDaemon(true);
        th.start();
    }

    public Parent getView() {
        return layout;
    }

    @Override
    public void onInput(InputEvent event) {
        /*if (event instanceof AxisControlEvent) {
            AxisControlEvent e = (AxisControlEvent) event;
            setYawAndTrust(e.getYaw(), e.getTrust());
            setRollAndPitch(e.getRoll(), e.getPitch());
            return;
        }
        if (event instanceof AltHoldEvent) {
            AltHoldEvent e = (AltHoldEvent) event;
            //xyColor = e.isHold() ? java.awt.Color.RED : java.awt.Color.BLACK;
            return;
        }*/
    }

    @Override
    public void valuesReceived(String name, Map<String, Object> values) {
        if (values.containsKey("stabilizer.roll")) {
            horizon.setRoll(-(Float) values.get("stabilizer.roll"));
        }
        if (values.containsKey("stabilizer.pitch")) {
            horizon.setPitch((Float) values.get("stabilizer.pitch"));
        }
        if (values.containsKey("baro.aslLong")) {
            altimeter.setValue((Float) values.get("baro.aslLong"));
        }
        if (verticalspeedmeter != null && values.containsKey("acc.z")) {
            verticalspeedmeter.setMinValue(-2.5f);
            verticalspeedmeter.setMaxValue(2.5f);
            //verticalspeedmeter.setDecimals(2);
            verticalspeedmeter.setInteractive(false);
            //verticalspeedmeter.setMajorTickSpace(0.01f);
            verticalspeedmeter.setNeedleColor(Color.BLACK);
            //verticalspeedmeter.setStartAngle(45.0f);
            float f = (float) values.get("acc.z");
            double value = -f;
            verticalspeedmeter.setValue(value);
            //System.out.println(value);
        }
        if (values.containsKey("mag.x") && values.containsKey("mag.y") && values.containsKey("mag.z")) {
            float x = (float) values.get("mag.x");
            float y = (float) values.get("mag.y");
            float z = (float) values.get("mag.z");
            // calculate tilt-compensated heading angle
            double cosRoll = Math.cos(Math.toRadians(horizon.getRoll()));
            double sinRoll = Math.sin(Math.toRadians(horizon.getRoll()));
            double cosPitch = Math.cos(Math.toRadians(horizon.getPitch()));
            double sinPitch = Math.sin(Math.toRadians(horizon.getPitch()));
            double Xh = x * cosPitch + z * sinPitch;
            double Yh = x * sinRoll * sinPitch + y * cosRoll - z * sinRoll * cosPitch;
            double heading = Math.atan2(Yh, Xh);
            double d_heading = Math.toDegrees(heading);
            compass.setBearing(d_heading);
        }
    }
}
