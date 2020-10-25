/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */

package ga.abzzezz.util;

/**
 * Little class to quickly log information to the console (as the class name suggests)
 */
public class QuickLog {
    /**
     * Log to console
     *
     * @param log     String to log
     * @param logType Type to quickly identify
     */
    public static void log(final String log, final LogType logType) {
        System.out.println(logType.name().concat("> " + log));
    }

    public enum LogType {
        ERROR, WARNING, INFO, SAVING, READING
    }
}
