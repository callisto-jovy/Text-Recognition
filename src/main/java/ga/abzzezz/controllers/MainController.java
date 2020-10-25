/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */


package ga.abzzezz.controllers;

import com.fazecast.jSerialComm.SerialPort;
import ga.abzzezz.Singleton;
import ga.abzzezz.config.ConfigHandler;
import ga.abzzezz.util.QuickLog;
import ga.abzzezz.util.SettingsHolder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

/**
 * Standard javafx controller class.
 */
public class MainController {

    @FXML
    private Slider xAxisSlider;
    @FXML
    private Slider yAxisSlider;
    @FXML
    private TextField xAxisField;
    @FXML
    private TextField yAxisField;
    @FXML
    private Button xAxisSubmit;
    @FXML
    private Button yAxisSubmit;
    @FXML
    private ComboBox<SerialPort> portComboBox;
    @FXML
    private CheckBox logResultsToFile;
    @FXML
    private TextField camIndexField;


    /**
     * Method adds items and restores saves
     */
    @FXML
    public void initialize() {
        portComboBox.getItems().addAll(SerialPort.getCommPorts());
        Singleton.INSTANCE.getSerialHandler().getSerialPort().ifPresent(serialPort -> setEnabled(serialPort.isOpen()));
        /* UI Components */
        xAxisSlider.setValue(Singleton.INSTANCE.getRotationHandler().getX());
        yAxisSlider.setValue(Singleton.INSTANCE.getRotationHandler().getY());

        Singleton.INSTANCE.getSerialHandler().getSerialPort().ifPresent(serialPort -> portComboBox.setValue(serialPort));

        logResultsToFile.setSelected(SettingsHolder.logResultsToFile);
    }

    /**
     * Sets the new port
     */
    @FXML
    public void onPortSelected() {
        setEnabled(Singleton.INSTANCE.getSerialHandler().setPort(portComboBox.getValue()));
    }

    /**
     * @param state state to set components disabled
     */
    private void setEnabled(boolean state) {
        state = !state;
        xAxisSubmit.setDisable(state);
        yAxisSubmit.setDisable(state);
        xAxisSlider.setDisable(state);
        yAxisSlider.setDisable(state);
        xAxisField.setDisable(state);
        yAxisField.setDisable(state);
    }
    /**
     * Method that is called after the mouse on the Y-Slider has been released
     */
    @FXML

    public void onYAxisChanged() {
        final int value = Singleton.INSTANCE.getSerialHandler().changeYAxis((int) yAxisSlider.getValue());
        yAxisField.setText(String.valueOf(value));
    }

    /**
     * Method that is called after the mouse on the X-Slider has been released
     */
    @FXML
    public void onXAxisChanged() {
        final int value = Singleton.INSTANCE.getSerialHandler().changeXAxis((int) xAxisSlider.getValue());
        xAxisField.setText(String.valueOf(value));
    }

    /**
     * Changes the x axis accordingly to the text-field
     */
    @FXML
    public void changeXAxis() {
        final int value = Singleton.INSTANCE.getSerialHandler().changeXAxis(Integer.parseInt(xAxisField.getText()));
        xAxisSlider.setValue(value);
    }

    /**
     * Changes the y axis accordingly to the text-field
     */
    @FXML
    public void changeYAxis() {
        final int value = Singleton.INSTANCE.getSerialHandler().changeYAxis(Integer.parseInt(yAxisField.getText()));
        yAxisSlider.setValue(value);
    }

    @FXML
    public void onTabViewChanged(final ActionEvent event) {
        try {
            final Parent configs = FXMLLoader.load(getClass().getResource("/configcreator.fxml"));
            final Scene scene = new Scene(configs, 1280, 800);
            final Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        } catch (final Exception e) {
            QuickLog.log("Switching scene", QuickLog.LogType.ERROR);
            e.printStackTrace();
        }
    }

    /* Settings */

    @FXML
    public void changeLogFile() {
        SettingsHolder.logResultsToFile = logResultsToFile.isSelected();
    }

    @FXML
    public void setCamIndex() {
        Singleton.INSTANCE.getProcessingHandler().setCamIndex(Integer.parseInt(camIndexField.getText()));
    }

    @FXML
    public void openLogs() {
        try {
            Desktop.getDesktop().open(Singleton.INSTANCE.getProcessedFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
