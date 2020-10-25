/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */


package ga.abzzezz.config;

import ga.abzzezz.Singleton;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.QuickLog;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Point;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Handler to store current configs, create new ones, etc.
 */
public class ConfigHandler {
    /**
     * Mode for servos, so when loaded the according code is run
     */
    public static final int SERVO_MODE = 0;
    /**
     * Mode to represent vertex points
     */
    public static final int IMAGE_VERTEX_MODE = 1;
    /**
     * Mode for image threshold configs
     */
    public static final int IMAGE_THRESHOLD_MODE = 2;
    /**
     * Mode to load all
     */
    public static final int ALL_MODE = 8;
    /**
     * List of all current, loaded configs
     */
    private final List<Config> configs = new ArrayList<>();

    /**
     * Name of the current config
     */
    private String currentConfig;

    /**
     * Iterates over all files in the app's config directory. Reads the config and adds it to the config list
     */
    public void loadConfigs() {
        try {
            if (!Singleton.INSTANCE.getConfigDir().exists())
                Singleton.INSTANCE.getConfigDir().mkdir();
            QuickLog.log("Configs", QuickLog.LogType.READING);
            final Optional<File[]> files = Optional.ofNullable(Singleton.INSTANCE.getConfigDir().listFiles());
            files.ifPresent(files1 -> {
                for (final File file : files1) {
                    final Config readConfig = readConfig(FileUtil.getFileContentsAsString(file));
                    configs.add(readConfig);
                }
            });
        } catch (final IllegalAccessError e) {
            QuickLog.log("Creating config directory. No configs will be available", QuickLog.LogType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Create a config with current servo positions
     *
     * @param name      config name
     * @param rotations the servo's current position
     * @return auto created config
     */
    public Config createServoConfig(final String name, final int[] rotations) {
        final JSONArray struct = new JSONArray();
        struct.put(new JSONObject().put("rotX", rotations[0]).put("rotY", rotations[1]));
        return createConfig(name, SERVO_MODE, struct);
    }

    /**
     * Create a config with points, each one representing one vertex
     *
     * @param name config name
     * @return auto created config
     */
    public Config createPointConfig(final String name) {
        final JSONArray struct = new JSONArray();
        final JSONArray pointsInArray = new JSONArray();
        for (final Point point : Singleton.INSTANCE.getVertexHandler().getPoints()) {
            final JSONObject pointObject = new JSONObject().put("x", point.x).put("y", point.y);
            pointsInArray.put(pointObject);
        }
        struct.put(pointsInArray);
        return createConfig(name, IMAGE_VERTEX_MODE, struct);
    }

    /**
     * Create threshold config
     *
     * @param name config name
     * @return auto created config
     */
    public Config createThresholdConfig(final String name) {
        final JSONArray struct = new JSONArray();
        struct.put(new JSONObject().put("thresh1", Singleton.INSTANCE.getProcessingHandler().getThresholds()[0]).put("thresh2", Singleton.INSTANCE.getProcessingHandler().getThresholds()[1]));
        return createConfig(name, IMAGE_THRESHOLD_MODE, struct);
    }

    /**
     * Create config for all modes
     *
     * @param name      config name
     * @param rotations rotations for servo config
     * @return config created from all
     */
    public Config createAllConfig(final String name, final int[] rotations) {
        final JSONArray struct = new JSONArray();
        struct.put(writeConfig0(createPointConfig(name))).put(writeConfig0(createServoConfig(name, rotations))).put(writeConfig0(createThresholdConfig(name)));
        return createConfig(name, ALL_MODE, struct);
    }

    /**
     * Called when a config should be loaded, set current config name & proceed to load as usual
     *
     * @param config config to be loaded
     */
    public void loadConfig(final Config config) {
        currentConfig = config.getName();
        loadConfig0(config);
    }

    /**
     * Base method to load a config
     * Executes code dependent on the config's mode
     *
     * @param config config to be loaded
     */
    private void loadConfig0(final Config config) {
        switch (config.getMode()) {
            case SERVO_MODE:
                for (final Object o : config.getContent()) {
                    final JSONObject jsonObject = new JSONObject(o.toString());
                    Singleton.INSTANCE.getSerialHandler().changeXAxis(jsonObject.getInt("rotX"));
                    Singleton.INSTANCE.getSerialHandler().changeYAxis(jsonObject.getInt("rotY"));
                }
                break;
            case IMAGE_VERTEX_MODE:
                for (final Object content : config.getContent()) {
                    final JSONArray pointArray = new JSONArray(content.toString());
                    for (int i = 0; i < pointArray.length(); i++) {
                        final JSONObject pointJson = pointArray.getJSONObject(i);
                        try {
                            Singleton.INSTANCE.getVertexHandler().addPoint(i, new Point(pointJson.getDouble("x"), pointJson.getDouble("y")));
                        } catch (final ArrayIndexOutOfBoundsException outOfBoundsException) {
                            QuickLog.log("More indices in saved array than possible", QuickLog.LogType.ERROR);
                        }
                    }
                }
                Singleton.INSTANCE.getVertexHandler().move();
                break;
            case IMAGE_THRESHOLD_MODE:
                for (final Object content : config.getContent()) {
                    final JSONObject jsonObject = new JSONObject(content.toString());
                    Singleton.INSTANCE.getProcessingHandler().setThreshold1(jsonObject.getDouble("thresh1"));
                    Singleton.INSTANCE.getProcessingHandler().setThreshold2(jsonObject.getDouble("thresh2"));
                }
                break;
            case ALL_MODE:
                for (final Object content : config.getContent()) {
                    loadConfig(readConfig(content.toString()));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Read string, convert to JSON etc. then retrieve information & return config
     *
     * @param lines data in
     * @return config from retrieved information
     */
    public Config readConfig(final String lines) {
        final JSONObject jsonObject = new JSONObject(lines);
        final JSONObject metaData = jsonObject.getJSONObject("meta");
        return new Config(metaData.getString("name"), metaData.getInt("mode"), jsonObject.getJSONArray("struct"));
    }

    /**
     * Create config based on parameters
     *
     * @param name   config name
     * @param mode   config mode for decoding
     * @param struct json array to be iterated upon
     * @return built config
     */
    private Config createConfig(final String name, final int mode, final JSONArray struct) {
        return new Config(name, mode, struct);
    }

    /**
     * Convert config to JSON and to string
     *
     * @param config Config to be parsed
     * @return JSON string
     */
    private String writeConfig(final Config config) {
        return new JSONObject().put("meta", new JSONObject().put("name", config.getName()).put("mode", config.getMode())).put("struct", config.getContent()).toString();
    }

    /**
     * Convert config to JSON
     *
     * @param config Config to be parsed
     * @return JSON string
     */
    private JSONObject writeStruct(final Config config) {
        return new JSONObject().put("struct", config.getContent());
    }

    /**
     * Convert config to JSON
     *
     * @param config Config to be parsed
     * @return JSON string
     */
    private JSONObject writeConfig0(final Config config) {
        return new JSONObject().put("meta", new JSONObject().put("name", config.getName()).put("mode", config.getMode())).put("struct", config.getContent());
    }

    /**
     * Save config in the config directory with a random uuid
     *
     * @param config config to be saved
     */
    public void saveConfig(final Config config) {
        QuickLog.log("Config to file", QuickLog.LogType.SAVING);
        final File configFile = new File(Singleton.INSTANCE.getConfigDir(), UUID.randomUUID().toString() + ".config");
        FileUtil.writeStringToFile(configFile, writeConfig(config), false);
        QuickLog.log("Config saved", QuickLog.LogType.INFO);
        loadConfigs();
    }

    /**
     * Shows a input dialog and returns a optional containing the text field's text
     *
     * @return Optional of text field's text
     */
    public Optional<String> showDialogConfigName() {
        final String response = JOptionPane.showInputDialog("Config name");
        if (response != null && !response.isEmpty())
            return Optional.of(response);
        else
            return Optional.empty();
    }

    /**
     * Shows a input dialog with all available configs, collected with a predicate
     *
     * @param predicate predicate to filter
     * @return user selected config
     */
    public Optional<Config> showAvailableConfigs(final Predicate<Config> predicate) {
        final Config[] configs = Singleton.INSTANCE.getConfigHandler().getConfigs().stream().filter(predicate).toArray(Config[]::new);
        //TODO: Add list selection
        final String strings = Arrays.stream(configs).map(Config::getName).collect(Collectors.joining("\n"));
        final String response = JOptionPane.showInputDialog(null, strings + "\nChoose from 0 - " + (configs.length - 1));
        if (response != null && !response.isEmpty())
            return Optional.of(configs[Integer.parseInt(response)]);
        else
            return Optional.empty();
    }

    /**
     * @return list of all configs
     */
    public List<Config> getConfigs() {
        return configs;
    }

    /**
     * @return current loaded config name
     */
    public String getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(String currentConfig) {
        this.currentConfig = currentConfig;
    }
}
