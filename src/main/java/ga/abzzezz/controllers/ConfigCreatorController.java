/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.controllers;

import ga.abzzezz.Main;
import ga.abzzezz.config.ConfigHandler;
import ga.abzzezz.util.QuickLog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.opencv.core.Point;

import java.util.Objects;

public class ConfigCreatorController {

    /**
     * Polygon with current vertices
     */
    private Polygon polygon;
    /**
     * Variables
     */
    @FXML
    private ImageView imageView;
    @FXML
    private Button startCaptureButton;
    @FXML
    private TextField threshold1Field;
    @FXML
    private TextField threshold2Field;
    @FXML
    private AnchorPane pane;
    @FXML
    private TextArea output;

    /**
     * Capture started indicator
     */
    private boolean capture;
    /**
     * Index for the current point to be set
     */
    private int pointIndex;

    /**
     * Default jfx initialize method.
     * Sets text and adds the polygon to the pane.
     * Furthermore sets mouse handler for the imageview
     */
    @FXML
    public void initialize() {
        threshold1Field.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[0]));
        threshold2Field.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[1]));
        /* Create polygon and define attributes */
        this.polygon = new Polygon();
        polygon.setFill(Color.TRANSPARENT);
        polygon.setStroke(Color.GREEN);
        polygon.setStrokeWidth(4);
        pane.getChildren().add(polygon);

        imageView.setOnMouseClicked(mouseEvent -> {
            if (capture) return;
            final double xPos = mouseEvent.getX(), yPos = mouseEvent.getY();

            if (pointIndex == -1) {
                clearPoints();
                pointIndex++;
            }

            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                polygon.getPoints().addAll(xPos, yPos);
                Main.INSTANCE.getVertexHandler().addPoint(pointIndex, new Point(xPos, yPos));
                pointIndex++;
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                if (polygon.getPoints().size() > 0)
                    polygon.getPoints().remove(polygon.getPoints().size() - 1);
            }

            /*
             * If first polygon is completed & new ones are selected, delete old polygon
             */
            if (pointIndex >= 4) {
                Main.INSTANCE.getVertexHandler().move();
                pointIndex = -1;
                QuickLog.log("Stored last four points", QuickLog.LogType.INFO);
            }
        });
    }

    /**
     * Analyse the current Image
     */
    @FXML
    public void doAnalyse() {
        output.appendText(Main.INSTANCE.getProcessingHandler().doOCR(imageView.getImage()));
    }

    /**
     * Start video capture and stop it (if already ´started)
     */
    @FXML
    public void onCapture() {
        capture = !capture;
        startCaptureButton.setText(capture ? "Stop Capture" : "Start capture");
        if (capture) {
            clearPolygon();
            imageView.setImage(null);
            Main.INSTANCE.getProcessingHandler().start(imageView);
        } else {
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

    /**
     * Save config
     */
    @FXML
    public void saveConfig() {
        movePolygons(false);
        Main.INSTANCE.getConfigHandler().showDialogConfigName().ifPresent(s -> Main.INSTANCE.getConfigHandler().saveConfig(Main.INSTANCE.getConfigHandler().createAllConfig(s, Main.INSTANCE.getRotationHandler().getCurrentRotations())));
    }

    /**
     * Load config
     */
    @FXML
    public void loadConfig() {
        Main.INSTANCE.getConfigHandler().showAvailableConfigs(config -> config.getMode() == ConfigHandler.ALL_MODE).ifPresent(response -> {
            clearPoints();
            Main.INSTANCE.getConfigHandler().loadConfig(response);
            for (final Point point : Main.INSTANCE.getVertexHandler().getPoints()) {
                polygon.getPoints().addAll(point.x, point.y);
            }

            threshold1Field.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[0]));
            threshold2Field.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[1]));
        });
    }

    /**
     * Change current threshold1
     */
    @FXML
    public void changeThreshold1() {
        threshold1Field.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().setThreshold1(Double.parseDouble(threshold1Field.getText()))));
    }

    /**
     * Change current threshold2
     */
    @FXML
    public void changeThreshold2() {
        threshold2Field.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().setThreshold2(Double.parseDouble(threshold2Field.getText()))));
    }

    @FXML
    public void clearPoints() {
        clearPolygon();
        Main.INSTANCE.getVertexHandler().clear();
        pointIndex = -1;
    }

    /**
     * Go back to main scene
     */
    @FXML
    public void onBack(final ActionEvent event) {
        try {
            final Parent configs = FXMLLoader.load(getClass().getResource("/main.fxml"));
            final Scene scene = new Scene(configs);
            final Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        } catch (final Exception e) {
            QuickLog.log("Switching scene", QuickLog.LogType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Refresh image with new processing
     */
    @FXML
    public void refreshImage() {
        clearPolygon();
        if (imageView == null || imageView.getImage() == null) return;
        Main.INSTANCE.getProcessingHandler().refreshProcessing(imageView);
    }

    /**
     * Moves all current polygon points to the vertex handler
     *
     * @param deleteOld should old points be deleted?
     */
    private void movePolygons(final boolean deleteOld) {
        if (deleteOld) clearPoints();
        for (int i = 1; i < polygon.getPoints().size(); i += 2) {
            Main.INSTANCE.getVertexHandler().addPoint(i / 2, new Point(polygon.getPoints().get(i - 1), polygon.getPoints().get(i)));
        }
    }


    /**
     * Clears all polygon points
     */
    private void clearPolygon() {
        polygon.getPoints().removeIf(Objects::nonNull);
    }

}
