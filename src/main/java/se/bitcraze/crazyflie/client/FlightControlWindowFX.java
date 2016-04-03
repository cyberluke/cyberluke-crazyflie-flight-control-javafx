package se.bitcraze.crazyflie.client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.client.ui.FlightDataPanelFX;

/**
 * Created by Lukas on 4/20/14.
 */
public class FlightControlWindowFX {

    private Scene defaultScene;
    private FlightDataPanelFX defaultDataPanel;
    private boolean play = false;

    public FlightControlWindowFX() {
        defaultDataPanel = new FlightDataPanelFX();
        defaultScene = new Scene(defaultDataPanel.getView());
    }

    public FlightDataPanelFX getDefaultDataPanel() {
        return defaultDataPanel;
    }

    public Scene getDefaultScene() {
        return defaultScene;
    }

    public boolean isPlay() {
        return play;
    }
}
