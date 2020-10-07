/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.controllers;

import ga.abzzezz.Main;
import ga.abzzezz.config.Config;
import ga.abzzezz.config.ConfigHandler;
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
import java.util.stream.Collectors;

public class ConfigCreatorController {

    /**
     * List of all rectangles with their points
     */
    private final List<Polygon> polygons = new ArrayList<>();
    private Polygon polygon;

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
    private int pointIndex;

    @FXML
    public void initialize() {
        threshold1.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[0]));
        threshold2.setText(String.valueOf(Main.INSTANCE.getProcessingHandler().getThresholds()[1]));

        imageView.setOnMouseClicked(mouseEvent -> {
            final double xPos = mouseEvent.getX(), yPos = mouseEvent.getY();
            if (pointIndex == 0) {
                polygon = new Polygon();
                polygon.setFill(Color.TRANSPARENT);
                polygon.setStroke(Color.GREEN);
                polygon.setStrokeWidth(4);
                pane.getChildren().add(polygon);
            }

            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                polygon.getPoints().addAll(xPos, yPos);
                pointIndex++;
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                if (polygon.getPoints().size() > 0)
                    polygon.getPoints().remove(polygon.getPoints().size() - 1);
            }

            if (pointIndex >= 4) {
                polygons.add(polygon);
                pointIndex = 0;
                QuickLog.log("Stored last four points", QuickLog.LogType.INFO);
            }
        });
    }

    /**
     * Start video capture and stop it (if already ´started)
     */
    @FXML
    public void onCapture() {
        capture = !capture;
        startCapture.setText(capture ? "Stop Capture" : "Start capture");
        if (capture) {
            if (!polygons.isEmpty()) {
                Main.INSTANCE.getVertexHandler().getPoints().addAll(movePolygons(true));
            }
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
        Main.INSTANCE.getConfigHandler().saveConfig(Main.INSTANCE.getConfigHandler().createPointConfig(JOptionPane.showInputDialog("Config name"), movePolygons(false)));
    }

    /**
     * Load config
     */
    @FXML
    public void loadConfig() {
        final Config[] configs = Main.INSTANCE.getConfigHandler().getConfigs().stream().filter(config -> config.getMode() == ConfigHandler.IMAGE_VERTEX_MODE).toArray(Config[]::new);
        //TODO: Add list selection
        final String strings = Arrays.stream(configs).map(Config::getName).collect(Collectors.joining("\n"));
        final String response = JOptionPane.showInputDialog(null, strings + "\nChoose from 0 - " + (configs.length - 1));
        if (response != null && !response.isEmpty()) {
            final int index = Integer.parseInt(response);
            Main.INSTANCE.getConfigHandler().loadConfig(configs[index]);
            polygons.clear();
            pane.getChildren().removeIf(node -> node.getClass().equals(Polygon.class));

            Main.INSTANCE.getVertexHandler().getPoints().forEach(pointArray -> {
                final Polygon polygon = new Polygon();
                polygon.setFill(Color.TRANSPARENT);
                polygon.setStroke(Color.GREEN);
                polygon.setStrokeWidth(4);
                for (final Point point : pointArray) {
                    polygon.getPoints().addAll(point.x, point.y);
                }
                pane.getChildren().add(polygon);
            });
        }
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
        polygons.clear();
        pane.getChildren().removeIf(node -> node.getClass().equals(Polygon.class));
        Main.INSTANCE.getVertexHandler().getPoints().clear();
    }

    /**
     * Go back to main scene
     */
    @FXML
    public void onBack() {
        //TODO: Go back
    }

    private List<Point[]> movePolygons(final boolean deleteOld) {
        final List<Point[]> points = new ArrayList<>();
        for (final Polygon iterPolygon : polygons) {
            final Point[] polygonPoints = new Point[4];
            for (int i = 1; i < iterPolygon.getPoints().size(); i += 2) {
                polygonPoints[i / 2] = new Point(iterPolygon.getPoints().get(i - 1), iterPolygon.getPoints().get(i));
            }
            points.add(polygonPoints);
        }
        if (deleteOld) polygons.clear();
        return points;
    }

}
