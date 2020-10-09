/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz;

import nu.pattern.OpenCV;

public class MainClass {
    /**
     * Default java main method.
     * Loads opencv's libraries and starts the main application
     *
     * @param args given arguments
     */
    public static void main(final String[] args) {
        OpenCV.loadLocally();
        Main.INSTANCE.setup(args);
    }
}
