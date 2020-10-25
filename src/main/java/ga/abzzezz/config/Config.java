/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */

package ga.abzzezz.config;

import org.json.JSONArray;

/**
 * Default config class
 * Contains a config name, it's mode (to later deserialize) and a JSON array containing further nested objects and values
 */
public class Config {

    private final String name;
    private final int mode;
    private final JSONArray content;

    /**
     * Default config constructor
     *
     * @param name    config name
     * @param mode    mode automatically chosen
     * @param content JSONArray containing all objects etc.
     */
    public Config(final String name, final int mode, final JSONArray content) {
        this.name = name;
        this.mode = mode;
        this.content = content;
    }

    /**
     * Get a json array with all the contents
     *
     * @return JSON array
     */
    public JSONArray getContent() {
        return content;
    }

    /**
     * @return config name
     */
    public String getName() {
        return name;
    }

    /**
     * @return integer representing the config mode
     */
    public int getMode() {
        return mode;
    }
}
