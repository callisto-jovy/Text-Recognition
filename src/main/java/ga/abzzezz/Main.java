package ga.abzzezz;

import com.fazecast.jSerialComm.SerialPort;
import ga.abzzezz.image.ProcessingHandler;
import ga.abzzezz.rotation.RotationHandler;
import ga.abzzezz.serial.SerialHandler;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.SettingsHolder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nu.pattern.OpenCV;
import org.json.JSONObject;

import java.io.File;

public class Main extends Application {

    /** Basic instance **/
    public static final Main INSTANCE = new Main();

    /* Handlers */

    /**
     * Handler for the serial transfer between the single board computer & the application
     **/
    private final SerialHandler serialHandler = new SerialHandler();
    /**
     * Handler for the servos rotations
     **/
    private final RotationHandler rotationHandler = new RotationHandler();
    /**
     * Handler to handle the image processing
     **/
    private final ProcessingHandler processingHandler = new ProcessingHandler();
    /**
     * File for main directory
     **/
    private final File mainDir = new File(System.getProperty("user.home"), "App");
    private final File savedFile = new File(mainDir, "saved_data.txt");
    private final File processedFile = new File(mainDir, "processed_strings.txt");

    public void setup(final String[] mainArgs) {
        if (!mainDir.exists()) mainDir.mkdirs();
        if (savedFile.exists()) {
            final JSONObject jsonObject = new JSONObject(FileUtil.getFileContentsAsString(savedFile));
            /* Read from file */
            final int rotX = jsonObject.getInt("rotX");
            final int rotY = jsonObject.getInt("rotY");
            final int port = jsonObject.getInt("port");
            getRotationHandler().setX(rotX);
            getRotationHandler().setY(rotY);
            getSerialHandler().setIndex(port);
            if(port != -1)
            getSerialHandler().setPort(SerialPort.getCommPorts()[port]);
            SettingsHolder.logResultsToFile = jsonObject.getBoolean("logResultsToFile");
        }
        launch(mainArgs);
    }

    /**
     * Start method for java FX application, set stage, etc.
     *
     * @param primaryStage stage to apply children to
     * @throws Exception if something goes wrong during the process
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("Text recognition controller");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setOnCloseRequest(windowEvent -> {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("rotX", getRotationHandler().getX()).put("rotY", getRotationHandler().getY()).put("port", getSerialHandler().getIndex());
            jsonObject.put("logResultsToFile", SettingsHolder.logResultsToFile);
            FileUtil.writeStringToFile(savedFile, jsonObject.toString(), false);

            getProcessingHandler().stop();
            System.exit(0);
        });
        primaryStage.show();
    }

    /* Getters and Setters */

    public SerialHandler getSerialHandler() {
        return serialHandler;
    }

    public File getMainDir() {
        return mainDir;
    }

    public File getSavedFile() {
        return savedFile;
    }

    public RotationHandler getRotationHandler() {
        return rotationHandler;
    }

    public ProcessingHandler getProcessingHandler() {
        return processingHandler;
    }

    public File getProcessedFile() {
        return processedFile;
    }
}
