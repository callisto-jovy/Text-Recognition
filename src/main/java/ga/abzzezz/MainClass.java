package ga.abzzezz;

import ga.abzzezz.rotation.RotationHandler;
import ga.abzzezz.serial.SerialHandler;
import ga.abzzezz.util.FileUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nu.pattern.OpenCV;
import org.json.JSONObject;

import java.io.File;

public class MainClass extends Application {

    public static boolean running;

    /**
     * Simple main method. Also registers shutdown hook to save latest rotations to.
     *
     * @param args java args[] in
     */
    public static void main(final String[] args) {
        OpenCV.loadShared();
        launch(args);
    }

    /**
     * Start method for java FX application, set stage, etc.
     *
     * @param primaryStage stage to apply children to
     * @throws Exception if something goes wrong during the process
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("Text recognition controller");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setOnCloseRequest(windowEvent -> {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("rotX", RotationHandler.INSTANCE.getX()).put("rotY", RotationHandler.INSTANCE.getY()).put("port", SerialHandler.INSTANCE.getIndex());
            FileUtil.writeStringToFile(new File(System.getProperty("user.home"), "Save.SAVE"), jsonObject.toString(), false);
            running = false;
        });
        primaryStage.show();
        running = true;
    }
}
