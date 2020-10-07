/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.config;

import org.json.JSONArray;

/**
 * Default config class
 */
public class Config {

    private final String name;
    private final int mode;
    private final JSONArray content;

    /**
     * Default config constructor
     * @param name config name
     * @param mode mode automatically chosen
     * @param content JSONArray containing all objects etc.
     */
    public Config(final String name, final int mode, final JSONArray content) {
        this.name = name;
        this.mode = mode;
        this.content = content;
    }

    public JSONArray getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public int getMode() {
        return mode;
    }
}
