/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */

package ga.abzzezz;

import com.fazecast.jSerialComm.SerialPort;
import ga.abzzezz.config.ConfigHandler;
import ga.abzzezz.image.ProcessingHandler;
import ga.abzzezz.rotation.RotationHandler;
import ga.abzzezz.serial.SerialHandler;
import ga.abzzezz.setting.SettingsHandler;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.QuickLog;
import ga.abzzezz.util.SettingsHolder;
import ga.abzzezz.vertex.VertexHandler;
import org.json.JSONObject;

import java.io.File;

public class Singleton {

    /**
     * Basic instance
     **/
    public static final Singleton INSTANCE = new Singleton();

    /**
     * File for main directory
     **/
    private final File mainDir = new File(System.getProperty("user.home"), "App");
    /**
     * File to store quick settings such as last rotations and global settings
     */
    private final File savedFile = new File(mainDir, "saved_data.txt");
    /**
     * File to write OCR strings to
     */
    private final File processedFile = new File(mainDir, "processed_strings.txt");
    /**
     * Directory to save configs to
     */
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
     * Handler to read and apply configs
     */
    private final ConfigHandler configHandler = new ConfigHandler();
    /**
     * Handler to store and manipulate the current rect vertices
     */
    private final VertexHandler vertexHandler = new VertexHandler();
    /**
     * Handler for saving and reading settings
     */
    private final SettingsHandler settingsHandler = new SettingsHandler();

    /**
     * Setup method, load settings and launch application
     *
     * @param mainArgs java arguments passed on from the main method
     */
    public void setup(final String[] mainArgs) {
        /* Check if needed directories exist, if they don't create them */
        if (!mainDir.exists()) mainDir.mkdirs();
        if (!configDir.exists()) configDir.mkdir();
        /* Reload previous settings */
        if (savedFile.exists()) {
            QuickLog.log("From settings file", QuickLog.LogType.READING);
            final JSONObject jsonObject = new JSONObject(FileUtil.getFileContentsAsString(savedFile));
            /* Read from file */
            final int rotX = jsonObject.getInt("rotX");
            final int rotY = jsonObject.getInt("rotY");
            final int port = jsonObject.getInt("port");
            getRotationHandler().setX(rotX);
            getRotationHandler().setY(rotY);

            if (port > SerialPort.getCommPorts().length) {
                getSerialHandler().setIndex(port);
                getSerialHandler().setPort(SerialPort.getCommPorts()[port]);
            }

            SettingsHolder.logResultsToFile = jsonObject.getBoolean("logResultsToFile");
            getProcessingHandler().setThreshold1(jsonObject.getDouble("threshold1"));
            getProcessingHandler().setThreshold2(jsonObject.getDouble("threshold2"));
            getProcessingHandler().setCamIndex(jsonObject.getInt("camIndex"));
        }
        /* Load configs */
        getConfigHandler().loadConfigs();
        /* Launch Java FX application */
        new App().launch0(mainArgs);
    }

    public void shutdown() {
        getSettingsHandler().storeSettings();
        getProcessingHandler().stop();
        System.exit(0);
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

    public SettingsHandler getSettingsHandler() {
        return settingsHandler;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public File getConfigDir() {
        return configDir;
    }

    public VertexHandler getVertexHandler() {
        return vertexHandler;
    }
}
