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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
     * Iterates over all files in the app's config directory. Reads the config and adds it to it's list
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
     * @param name config name
     * @param positions the servo's current position
     * @return auto created config
     */
    public Config createServoConfig(final String name, final int[] positions) {
        final JSONArray struct = new JSONArray();
        struct.put(new JSONObject().put("rotX", positions[0]).put("rotY", positions[1]));
        return createConfig(name, SERVO_MODE, struct);
    }

    /**
     * Create a config with the purpose of
     * @param name config name
     * @param points list of opencv point arrays, each one representing a rectangle
     * @return auto created config
     */
    public Config createPointConfig(final String name, final List<Point[]> points) {
        final JSONArray struct = new JSONArray();
        points.forEach(points1 -> {
            final JSONArray pointsInArray = new JSONArray();
            for (final Point point : points1) {
                final JSONObject pointObject = new JSONObject().put("x", point.x).put("y", point.y);
                pointsInArray.put(pointObject);
            }
            struct.put(pointsInArray);
        });
        return createConfig(name, IMAGE_VERTEX_MODE, struct);
    }

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
                    final Point[] points = new Point[4];
                    for (int i = 0; i < pointArray.length(); i++) {
                        final JSONObject pointJson = pointArray.getJSONObject(i);
                        points[i] = new Point(pointJson.getDouble("x"), pointJson.getDouble("y"));
                    }
                    Main.INSTANCE.getVertexHandler().getPoints().add(points);
                }
                break;
            default:
                break;
        }
    }

    public Config readConfig(final String lines) {
        final JSONObject jsonObject = new JSONObject(lines);
        final JSONObject metaData = jsonObject.getJSONObject("meta");
        return new Config(metaData.getString("name"), metaData.getInt("mode"), jsonObject.getJSONArray("struct"));
    }

    private Config createConfig(final String name, final int mode, final JSONArray struct) {
        return new Config(name, mode, struct);
    }

    private String writeConfig(final Config config) {
        return new JSONObject().put("meta", new JSONObject().put("name", config.getName()).put("mode", config.getMode())).put("struct", config.getContent()).toString();
    }

    public void saveConfig(final Config config) {
        final File configFile = new File(Main.INSTANCE.getConfigDir(), UUID.randomUUID().toString() + ".config");
        FileUtil.writeStringToFile(configFile, writeConfig(config), false);
    }

    public List<Config> getConfigs() {
        return configs;
    }
}