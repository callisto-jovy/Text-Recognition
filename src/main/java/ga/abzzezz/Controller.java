package ga.abzzezz;

import com.fazecast.jSerialComm.SerialPort;
import ga.abzzezz.util.SettingsHolder;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

/**
 * Standard javafx controller class.
 */
public class Controller {
    @FXML
    public ImageView image;
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
    private Button startCapture;
    @FXML
    private ListView<String> foundText;
    @FXML
    private CheckBox logResultsToFile;
    @FXML
    private TextField threshold1;
    @FXML
    private TextField threshold2;

    private boolean capture;

    /**
     * Method adds items and restores saves
     */
    @FXML
    public void initialize() {
        portComboBox.getItems().addAll(SerialPort.getCommPorts());
        //    setDisable(Main.INSTANCE.getSerialHandler().getSerialPort().isOpen());
        /* UI Components */
        xAxisSlider.setValue(Main.INSTANCE.getRotationHandler().getX());
        yAxisSlider.setValue(Main.INSTANCE.getRotationHandler().getY());
        portComboBox.setValue(Main.INSTANCE.getSerialHandler().getSerialPort());
        threshold1.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[0]));
        threshold2.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[1]));

        logResultsToFile.setSelected(SettingsHolder.logResultsToFile);
    }

    @FXML
    public void onCaptureStarted() {
        capture = !capture;
        startCapture.setText(capture ? "Stop Capture" : "Start capture");
        if (capture)
            Main.INSTANCE.getProcessingHandler().start(image, foundText);
        else {
            Main.INSTANCE.getProcessingHandler().stop();
            image.setImage(null);
        }
    }

    /**
     * Sets the new port
     */
    @FXML
    public void onPortSelected() {
        setDisable(Main.INSTANCE.getSerialHandler().setPort(portComboBox.getValue()));
    }

    /**
     * @param state state to set components disabled
     */
    private void setDisable(boolean state) {
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
        final int value = Main.INSTANCE.getSerialHandler().changeYAxis((int) yAxisSlider.getValue());
        yAxisField.setText(String.valueOf(value));
    }

    /**
     * Method that is called after the mouse on the X-Slider has been released
     */
    @FXML
    public void onXAxisChanged() {
        final int value = Main.INSTANCE.getSerialHandler().changeXAxis((int) xAxisSlider.getValue());
        xAxisField.setText(String.valueOf(value));
    }

    /**
     * Changes the x axis accordingly to the text-field
     */
    @FXML
    public void changeXAxis() {
        final int value = Main.INSTANCE.getSerialHandler().changeXAxis(Integer.parseInt(xAxisField.getText()));
        xAxisSlider.setValue(value);
    }

    /**
     * Changes the y axis accordingly to the text-field
     */
    @FXML
    public void changeYAxis() {
        final int value = Main.INSTANCE.getSerialHandler().changeYAxis(Integer.parseInt(yAxisField.getText()));
        yAxisSlider.setValue(value);
    }

    /* Settings */

    @FXML
    public void changeLogFile() {
        SettingsHolder.logResultsToFile = logResultsToFile.isSelected();
    }


    @FXML
    public void changeThreshold1() {
        threshold1.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().setThreshold1(Double.parseDouble(threshold1.getText()))));
    }

    @FXML
    public void changeThreshold2() {
        threshold2.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().setThreshold2(Double.parseDouble(threshold2.getText()))));
    }
}
