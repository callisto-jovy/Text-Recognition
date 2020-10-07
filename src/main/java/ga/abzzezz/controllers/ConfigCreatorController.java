/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.controllers;

import ga.abzzezz.Main;
import ga.abzzezz.util.QuickLog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.opencv.core.Point;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigCreatorController {

    @FXML
    public ImageView imageView;
    @FXML
    private Button startCapture;
    @FXML
    private TextField threshold1;
    @FXML
    private TextField threshold2;
    @FXML
    private AnchorPane pane;
    /**
     * Capture started indicator
     */
    private boolean capture;
    /**
     * List of all rectangles with their points
     */
    private final List<Point[]> points = new ArrayList<>();
    private final Point[] localPoints = new Point[4];
    private final Polygon polygon = new Polygon();
    private int i;

    @FXML
    public void initialize() {
        threshold1.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[0]));
        threshold2.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[1]));

        polygon.setFill(Color.TRANSPARENT);
        polygon.setStroke(Color.GREEN);
        polygon.setStrokeWidth(4);
        pane.getChildren().add(polygon);

        imageView.setOnMouseClicked(mouseEvent -> {
            //final Image image = imageView.getImage();
            final double xPos = mouseEvent.getX(), yPos = mouseEvent.getY();

            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                localPoints[i] = new Point(xPos, yPos);
                polygon.getPoints().addAll(xPos, yPos);
                i++;
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                localPoints[i] = null;
            }

            if (i >= 4) {
                this.points.add(localPoints.clone());
                polygon.getPoints().clear();

                i = 0;
                QuickLog.log("Stored last four points", QuickLog.LogType.INFO);
            }
/*
            // Draw Points
            final WritableImage wi = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
            final PixelWriter pw = wi.getPixelWriter();
            pw.setColor(xPos, yPos, new Color(0, 1, 0, 1));
            imageView.setImage(wi);

 */
        });
    }

    /**
     * Start video capture and stop it (if already ´started)
     */
    @FXML
    public void onCapture() {
        capture = !capture;
        startCapture.setText(capture ? "Stop Capture" : "Start capture");
        if (capture)
            Main.INSTANCE.getProcessingHandler().start(imageView);
        else {
            Main.INSTANCE.getProcessingHandler().stop();
            imageView.setImage(null);
        }
    }

    /**
     * Triggered when take image button is pressed
     */
    @FXML
    public void onTakeImage() {
        Main.INSTANCE.getProcessingHandler().takeImage(imageView);
    }

    @FXML
    public void saveConfig() {
        Main.INSTANCE.getConfigHandler().saveConfig(Main.INSTANCE.getConfigHandler().createPointConfig(JOptionPane.showInputDialog("Config name"), points));
    }

    /**
     * Change current threshold1
     */
    @FXML
    public void changeThreshold1() {
        threshold1.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().setThreshold1(Double.parseDouble(threshold1.getText()))));
    }

    /**
     * Change current threshold2
     */
    @FXML
    public void changeThreshold2() {
        threshold2.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().setThreshold2(Double.parseDouble(threshold2.getText()))));
    }

    @FXML
    public void clearPoints() {
        polygon.getPoints().clear();
        Arrays.fill(localPoints, null);
        points.clear();
    }

    /**
     * Go back to main scene
     */
    @FXML
    public void onBack() {
        //TODO: Go back
    }

}
