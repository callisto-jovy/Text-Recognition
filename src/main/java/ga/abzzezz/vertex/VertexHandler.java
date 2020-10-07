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
     * List with point-arrays - one representing a whole rectangle
     */
    private final List<Point[]> points = new ArrayList<>();

    /**
     * @return point array
     */
    public List<Point[]> getPoints() {
        return points;
    }
}
