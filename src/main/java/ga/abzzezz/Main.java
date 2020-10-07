/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz;

import com.fazecast.jSerialComm.SerialPort;
import ga.abzzezz.config.ConfigHandler;
import ga.abzzezz.file.FileHandler;
import ga.abzzezz.image.ProcessingHandler;
import ga.abzzezz.rotation.RotationHandler;
import ga.abzzezz.serial.SerialHandler;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.QuickLog;
import ga.abzzezz.util.SettingsHolder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;

public class Main extends Application {

    /**
     * Basic instance
     **/
    public static final Main INSTANCE = new Main();

    /**
     * File for main directory
     **/
    private final File mainDir = new File(System.getProperty("user.home"), "App");
    private final File savedFile = new File(mainDir, "saved_data.txt");
    private final File processedFile = new File(mainDir, "processed_strings.txt");
    private final File configDir = new File(mainDir, "CFG");
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
     * Handler for saving and reading settings
     */
    private final FileHandler fileHandler = new FileHandler();
    /**
     * Handler to read and apply configs
     */
    private final ConfigHandler configHandler = new ConfigHandler();

    public void setup(final String[] mainArgs) {
        if (!mainDir.exists()) mainDir.mkdirs();
        if (!configDir.exists()) configDir.mkdir();
        if (savedFile.exists()) {
            QuickLog.log("From settings file", QuickLog.LogType.READING);
            final JSONObject jsonObject = new JSONObject(FileUtil.getFileContentsAsString(savedFile));
            /* Read from file */
            final int rotX = jsonObject.getInt("rotX");
            final int rotY = jsonObject.getInt("rotY");
            final int port = jsonObject.getInt("port");
            getRotationHandler().setX(rotX);
            getRotationHandler().setY(rotY);
            getSerialHandler().setIndex(port);
            if (port > SerialPort.getCommPorts().length)
                getSerialHandler().setPort(SerialPort.getCommPorts()[port]);

            SettingsHolder.logResultsToFile = jsonObject.getBoolean("logResultsToFile");
            getProcessingHandler().setThreshold1(jsonObject.getDouble("threshold1"));
            getProcessingHandler().setThreshold2(jsonObject.getDouble("threshold2"));
        }
        // getConfigHandler().saveConfig(getConfigHandler().createServoConfig("servotest1", getRotationHandler().getCurrentRotations()));
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
        primaryStage.setTitle("LCD ORC Cam controller");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setOnCloseRequest(windowEvent -> {
            QuickLog.log("Shutting down.", QuickLog.LogType.WARNING);
            getFileHandler().storeSettings();
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

    public FileHandler getFileHandler() {
        return fileHandler;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public File getConfigDir() {
        return configDir;
    }
}
