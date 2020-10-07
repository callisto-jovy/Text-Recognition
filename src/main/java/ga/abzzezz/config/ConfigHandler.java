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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConfigHandler {

    public static final int SERVO_MODE = 0;
    public static final int IMAGE_VERTEX_MODE = 1;
    public static final int IMAGE_THRESHOLD_MODE = 2;

    private final List<Config> configs = new ArrayList<>();

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

    public Config createServoConfig(final String name, final int[] positions) {
        final JSONArray struct = new JSONArray();
        struct.put(new JSONObject().put("rotX", positions[0]).put("rotY", positions[1]));
        return createConfig(name, SERVO_MODE, struct);
    }

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
