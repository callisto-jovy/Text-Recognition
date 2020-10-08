/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.vertex;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for the current vertices for image processing
 */
public class VertexHandler {
    /**
     * Point list, each one representing one corner
     */
    private final List<Point> points = new ArrayList<>();

    /**
     * @return point list
     */
    public List<Point> getPoints() {
        return points;
    }
}
