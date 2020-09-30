package ga.abzzezz;

import com.fazecast.jSerialComm.SerialPort;
import ga.abzzezz.rotation.RotationHandler;
import ga.abzzezz.serial.SerialHandler;
import ga.abzzezz.util.FileUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.sourceforge.tess4j.Tesseract;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Standard javafx controller class. Only handles the sliders for now.
 */
public class Controller {
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
    public ImageView image;
    @FXML
    private Button startCapture;
    @FXML
    private ListView<String> foundText;


    private boolean capture;
    private final VideoCapture videoCapture = new VideoCapture();
    private final Tesseract tesseract = new Tesseract();

    /**
     * Method adds items and restores saves
     */
    @FXML
    public void initialize() {
        portComboBox.getItems().addAll(SerialPort.getCommPorts());
        final File file = new File(System.getProperty("user.home"), "Save.SAVE");
        if (file.exists()) {
            final JSONObject jsonObject = new JSONObject(FileUtil.getFileContentsAsString(file));
            /* Read from file */
            RotationHandler.INSTANCE.setX(jsonObject.getInt("rotX"));
            RotationHandler.INSTANCE.setY(jsonObject.getInt("rotY"));
            setDisable(SerialHandler.INSTANCE.setPort(SerialPort.getCommPorts()[jsonObject.getInt("port")]));
            /* UI Components */
            xAxisSlider.setValue(RotationHandler.INSTANCE.getX());
            yAxisSlider.setValue(RotationHandler.INSTANCE.getY());
            portComboBox.setValue(SerialHandler.INSTANCE.getSerialPort());
        }
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("lets");
        tesseract.setTessVariable("user_defined_dpi", "300");
    }

    @FXML
    public void onCaptureStarted() {
        capture = !capture;
        startCapture.setText(capture ? "Stop Capture" : "Start capture");
        if (capture) {
            videoCapture.open(0);
            final Mat matrix = new Mat();
            new Thread(() -> {
                while (videoCapture.isOpened() && videoCapture.read(matrix) && MainClass.running && capture) {
                    /* Image processing */
                    final MatOfByte bytes = new MatOfByte();
                    final Mat matrixGray = new Mat();
                    Imgproc.cvtColor(matrix, matrixGray, Imgproc.COLOR_BGR2GRAY);
                    Imgcodecs.imencode(".jpg", matrixGray, bytes);
                    /* ************************** */

                    final Image image = new Image(new ByteArrayInputStream(bytes.toArray()));
                    this.image.setImage(image);

                    try {
                        final String found = tesseract.doOCR(SwingFXUtils.fromFXImage(image, new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_BYTE_GRAY)));
                        if (found != null && !found.isEmpty()) {
                            foundText.getItems().add(found);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            if (videoCapture.isOpened()) videoCapture.release();
            image.setImage(null);
        }
    }

    /**
     * Sets the new port
     */
    @FXML
    public void onPortSelected() {
        setDisable(SerialHandler.INSTANCE.setPort(portComboBox.getValue()));
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
        final int value = SerialHandler.INSTANCE.changeYAxis((int) yAxisSlider.getValue());
        yAxisField.setText(String.valueOf(value));
    }

    /**
     * Method that is called after the mouse on the X-Slider has been released
     */
    @FXML
    public void onXAxisChanged() {
        final int value = SerialHandler.INSTANCE.changeXAxis((int) xAxisSlider.getValue());
        xAxisField.setText(String.valueOf(value));
    }

    /**
     * Changes the x axis accordingly to the text-field
     */
    @FXML
    public void changeXAxis() {
        final int value = SerialHandler.INSTANCE.changeXAxis(Integer.parseInt(xAxisField.getText()));
        xAxisSlider.setValue(value);
    }

    /**
     * Changes the y axis accordingly to the text-field
     */
    @FXML
    public void changeYAxis() {
        final int value = SerialHandler.INSTANCE.changeYAxis(Integer.parseInt(yAxisField.getText()));
        yAxisSlider.setValue(value);
    }
}
