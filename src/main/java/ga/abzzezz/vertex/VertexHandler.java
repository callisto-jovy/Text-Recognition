/*
 * Created by Roman P.  (2020.)
 * created to work on Java version 8
 *
 *
 */

package ga.abzzezz.vertex;

import org.opencv.core.MatOfPoint;
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
    private final List<MatOfPoint> matOfPoints = new ArrayList<>();

    /**
     * @return point list
     */
    public List<Point> getPoints() {
        return points;
    }

    public void addPoint(final int index, final Point point) {
        getPoints().add(index, point);
    }

    public void clear() {
        getPoints().clear();
        getMatOfPoints().clear();
    }

    public List<MatOfPoint> getMatOfPoints() {
        return matOfPoints;
    }

    /**
     * Moves the points to a mat of points
     */
    public void move() {
        final MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromList(points);
        getMatOfPoints().add(matOfPoint);
    }
}
