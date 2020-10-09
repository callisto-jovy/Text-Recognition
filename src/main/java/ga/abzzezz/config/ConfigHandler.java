/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.config;

import ga.abzzezz.Main;
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
     * List of all current, loaded configs
     */
    private final List<Config> configs = new ArrayList<>();

    /**
     * Iterates over all files in the app's config directory. Reads the config and adds it to the config list
     */
    public void loadConfigs() {
        try {
            if (!Main.INSTANCE.getConfigDir().exists())
                Main.INSTANCE.getConfigDir().mkdir();
            QuickLog.log("Configs", QuickLog.LogType.READING);

            for (final File file : Main.INSTANCE.getConfigDir().listFiles()) {
                final Config readConfig = readConfig(FileUtil.getFileContentsAsString(file));
                configs.add(readConfig);
            }
        } catch (final IllegalAccessError e) {
            QuickLog.log("Creating config directory. No configs will be available", QuickLog.LogType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Create a config with current servo positions
     *
     * @param name      config name
     * @param positions the servo's current position
     * @return auto created config
     */
    public Config createServoConfig(final String name, final int[] positions) {
        final JSONArray struct = new JSONArray();
        struct.put(new JSONObject().put("rotX", positions[0]).put("rotY", positions[1]));
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
        for (final Point point : Main.INSTANCE.getVertexHandler().getPoints()) {
            final JSONObject pointObject = new JSONObject().put("x", point.x).put("y", point.y);
            pointsInArray.put(pointObject);
        }
        struct.put(pointsInArray);
        return createConfig(name, IMAGE_VERTEX_MODE, struct);
    }

    /**
     * Loads a config dependent on it's id
     *
     * @param config config to be loaded
     */
    public void loadConfig(final Config config) {
        switch (config.getMode()) {
            case SERVO_MODE:
                for (final Object o : config.getContent()) {
                    final JSONObject jsonObject = new JSONObject(o.toString());
                    Main.INSTANCE.getSerialHandler().changeXAxis(jsonObject.getInt("x"));
                    Main.INSTANCE.getSerialHandler().changeYAxis(jsonObject.getInt("y"));
                }
                break;
            case IMAGE_VERTEX_MODE:
                for (final Object content : config.getContent()) {
                    final JSONArray pointArray = new JSONArray(content.toString());
                    for (int i = 0; i < pointArray.length(); i++) {
                        final JSONObject pointJson = pointArray.getJSONObject(i);
                        try {
                            Main.INSTANCE.getVertexHandler().addPoint(i, new Point(pointJson.getDouble("x"), pointJson.getDouble("y")));
                        } catch (final ArrayIndexOutOfBoundsException outOfBoundsException) {
                            QuickLog.log("More indices in saved array than possible", QuickLog.LogType.ERROR);
                        }
                    }
                    Main.INSTANCE.getVertexHandler().move();
                }
                break;
            case IMAGE_THRESHOLD_MODE:
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
     * Save config in the config directory with a random uuid
     *
     * @param config config to be saved
     */
    public void saveConfig(final Config config) {
        QuickLog.log("Config to file", QuickLog.LogType.SAVING);
        final File configFile = new File(Main.INSTANCE.getConfigDir(), UUID.randomUUID().toString() + ".config");
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
        final Config[] configs = Main.INSTANCE.getConfigHandler().getConfigs().stream().filter(predicate).toArray(Config[]::new);
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
}
