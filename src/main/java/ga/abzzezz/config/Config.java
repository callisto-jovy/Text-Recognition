/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.config;

import org.json.JSONArray;

public class Config {

    private final String name;
    private final int mode;
    private final JSONArray content;


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
