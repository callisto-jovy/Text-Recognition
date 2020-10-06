package ga.abzzezz;

import nu.pattern.OpenCV;

public class MainClass {

    public static void main(final String[] args) {
        OpenCV.loadLocally();
        Main.INSTANCE.setup(args);
    }
}
