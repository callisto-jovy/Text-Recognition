/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */

import ga.abzzezz.Singleton;
import nu.pattern.OpenCV;

public class Main {
    /**
     * Default java main method.
     * Loads opencv's libraries and starts the main application
     *
     * @param args given arguments
     */
    public static void main(final String[] args) {
        OpenCV.loadLocally();
        Singleton.INSTANCE.setup(args);
    }
}
