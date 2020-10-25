/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */


package ga.abzzezz.util;

/**
 * Util to do quick math operations that are not part of the default java
 */
public class MathUtil {

    /**
     * Clamp given value to a min and a max
     *
     * @param value value to be clamped
     * @param min   min value can be
     * @param max   max value can be
     * @return clamped value
     */
    public static double clamp(final double value, final double min, final double max) {
        return value > max ? max : Math.max(value, min);
    }

}
