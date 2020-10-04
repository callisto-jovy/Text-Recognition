package ga.abzzezz;

import nu.pattern.OpenCV;

public class MainClass {

    public static void main(final String[] args) {
        OpenCV.loadShared();
        Main.INSTANCE.setup(args);
    }
}
