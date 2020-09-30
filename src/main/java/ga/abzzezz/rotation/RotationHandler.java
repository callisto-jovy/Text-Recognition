package ga.abzzezz.rotation;

/**
 * Handler class to store, compare and calculate rotations
 */
public class RotationHandler {
    /**
     * Simple instance
     */
    public static final RotationHandler INSTANCE = new RotationHandler();
    /**
     * Integer array containing the servo's current rotation set by the application.
     * 0 : x
     * 1 : y
     */
    private final int[] currentRotations = new int[2];

    public RotationHandler() {
    }

    /**
     * Test if there's no difference between the old and new x value
     *
     * @param x integer (n) to be tested against
     * @return if @param x is equal to the old x value
     */
    public boolean noDiffX(final int x) {
        return x == getX();
    }

    /**
     * Test if there's no difference between the old and new y value
     *
     * @param y integer (n) to be tested against
     * @return if @param y is equal to the old y value
     */
    public boolean noDiffY(final int y) {
        return y == getY();
    }

    /**
     * Set current rotation [0] to @param x
     *
     * @param x integer (n) to set current rotation [0] to
     * @return @param x
     */
    public int setX(final int x) {
        getCurrentRotations()[0] = x;
        return x;
    }

    /**
     * Set current rotation [1] to @param y
     *
     * @param y integer (n) to set current rotation [1] to
     * @return @param y
     */
    public int setY(final int y) {
        getCurrentRotations()[1] = y;
        return y;
    }

    /**
     * @return current rotation [1]
     */
    public int getY() {
        return getCurrentRotations()[1];
    }

    /**
     * @return current rotation [0]
     */
    public int getX() {
        return getCurrentRotations()[0];
    }

    /**
     * @return int array containing the current rot
     */
    public int[] getCurrentRotations() {
        return currentRotations;
    }
}
