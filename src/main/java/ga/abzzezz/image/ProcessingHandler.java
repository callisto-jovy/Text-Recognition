/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.image;

import ga.abzzezz.Main;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.MathUtil;
import ga.abzzezz.util.QuickLog;
import ga.abzzezz.util.SettingsHolder;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class to process camera data and do image processing
 */
public class ProcessingHandler {
    /**
     * Video capture for the webcam
     **/
    private final VideoCapture videoCapture = new VideoCapture();
    /**
     * Tesseract instance for the trained image recognition
     **/
    private final Tesseract tesseract = new Tesseract();
    /**
     * mat for the current received image
     **/
    private final Mat videoMat = new Mat(), imageMap = new Mat();
    /**
     * Service for video thread
     **/
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    /**
     * Custom thresholds
     */
    private final double[] thresholds = new double[2];
    /**
     * Camera index (integrated webcam usually no. 0)
     */
    private final int camIndex = 0;

    public ProcessingHandler() {
        QuickLog.log("Loading tesseract", QuickLog.LogType.INFO);
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("ssd");
        tesseract.setTessVariable("user_defined_dpi", "300");
        QuickLog.log("Done loading tesseract", QuickLog.LogType.INFO);
    }

    /**
     * Start the video capture, run thread and process image.
     *
     * @param imageView imageView to later display image
     */
    public void start(final ImageView imageView) {
        QuickLog.log("Starting video capture", QuickLog.LogType.INFO);
        videoCapture.open(camIndex);
        executorService.submit(new Thread(() -> {
            while (videoCapture.isOpened() && videoCapture.read(videoMat)) {
                final MatOfByte processedByteMat = new MatOfByte();
                final Mat copyMat = doProcessing(videoMat);

                Imgcodecs.imencode(".jpg", copyMat, processedByteMat);

                final byte[] bytes = processedByteMat.toArray();
                imageView.setImage(new Image(new ByteArrayInputStream(bytes)));


                processedByteMat.release();
                copyMat.release();
                videoMat.release();
            }
        }));
    }

    /**
     * Take a image with the current cam
     *
     * @param imageView imageView to later display image
     */
    public void takeImage(final ImageView imageView) {
        stop();
        QuickLog.log("Taking image", QuickLog.LogType.INFO);
        videoCapture.open(camIndex);
        executorService.submit(new Thread(() -> {
            videoCapture.read(imageMap);
            //TODO: Move to method
            final MatOfByte processedByteMat = new MatOfByte();
            final Mat copyMat = doProcessing(imageMap);

            Imgcodecs.imencode(".jpg", copyMat, processedByteMat);
            imageView.setImage(new Image(new ByteArrayInputStream(processedByteMat.toArray())));

            processedByteMat.release();
            copyMat.release();
            stop();
        }));
    }

    /**
     * Refresh current image, reapply processing
     *
     * @param imageView imageview to display image to
     */
    public void refreshProcessing(final ImageView imageView) {
        final MatOfByte processedByteMat = new MatOfByte();
        final Mat copyMat = doProcessing(imageMap);

        Imgcodecs.imencode(".jpg", copyMat, processedByteMat);
        imageView.setImage(new Image(new ByteArrayInputStream(processedByteMat.toArray())));

        processedByteMat.release();
        copyMat.release();
    }

    /**
     * Do image processing with src and dest
     *
     * @param src source mat to first apply grayscale to
     * @return destination (processed)
     */
    private Mat doProcessing(final Mat src) {
        Mat dest = new Mat();
        if (getRect().isPresent()) {
            final Rect rect = getRect().get();
            dest = new Mat(src, rect);

            Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY, 0);
            Imgproc.rectangle(dest, rect, new Scalar(0, 255, 0), 2);
            Imgproc.Canny(dest, dest, getThresholds()[0], getThresholds()[1]);

        } else {
            Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY, 0);
            Imgproc.Canny(dest, dest, getThresholds()[0], getThresholds()[1]);
        }
        return dest;
    }

    /**
     * Do tesseract OCR. Convert mat to buffered-image fist
     *
     * @param mat Mat to be converted and searched
     * @return Found string
     */
    public String doOCR(final Mat mat) {
        final BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
        final byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return doOCR(image);
    }

    /**
     * Do tesseract OCR on java ofx image
     *
     * @param image image to do guess on
     * @return guess
     */
    public String doOCR(final Image image) {
        return doOCR(SwingFXUtils.fromFXImage(image, new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_BYTE_GRAY)));
    }

    /**
     * Do Tesseract OCR from BufferedImage
     *
     * @param image buffered image
     * @return tesseract's guess
     */
    private String doOCR(final BufferedImage image) {
        try {
            Rectangle rectangle = null;
            if (getRect().isPresent()) {
                final Rect rect = getRect().get();
                rectangle = new Rectangle(getRect().get().x, rect.y, rect.width, rect.height);
            }
            final String tessGuess = tesseract.doOCR(image, rectangle);
            System.out.println("Guess:" + tessGuess);
            if (!tessGuess.isEmpty()) {
                if (SettingsHolder.logResultsToFile)
                    FileUtil.writeStringToFile(Main.INSTANCE.getProcessedFile(), tessGuess, true);
                return tessGuess;
            }
        } catch (final TesseractException e) {
            QuickLog.log("Doing OCR", QuickLog.LogType.ERROR);
            e.printStackTrace();
            return "";
        }
        return "";
    }

    /**
     * Stop the video recording and release all resources in use
     */
    public void stop() {
        QuickLog.log("Stopping video capture", QuickLog.LogType.WARNING);
        if (videoCapture.isOpened())
            videoCapture.release();
        try {
            if (executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                QuickLog.log("Shutting down threads", QuickLog.LogType.WARNING);
                executorService.shutdown();
            }
        } catch (final InterruptedException e) {
            QuickLog.log("Shutting down image process thread.", QuickLog.LogType.ERROR);
            e.printStackTrace();
        }
        videoMat.release();
    }

    /**
     * Return both thresholds
     *
     * @return double array
     */
    public double[] getThresholds() {
        return thresholds;
    }

    /**
     * Set the current threshold 1 to a given value. Given value is clamped between 0 & 255
     *
     * @param value value to set to
     * @return value
     */
    public double setThreshold1(double value) {
        value = MathUtil.clamp(value, 0, 255);
        getThresholds()[0] = value;
        return value;
    }

    /**
     * Set the current threshold 2 to a given value. Given value is clamped between 0 & 255
     *
     * @param value value to set to
     * @return value
     */
    public double setThreshold2(double value) {
        value = MathUtil.clamp(value, 0, 255);
        getThresholds()[1] = value;
        return value;
    }

    private Optional<Rect> getRect() {
        if (Main.INSTANCE.getVertexHandler().getPoints().size() == 4) {
            return Optional.of(new Rect(Main.INSTANCE.getVertexHandler().getPoints().get(0), Main.INSTANCE.getVertexHandler().getPoints().get(2)));
        } else return Optional.empty();
    }
}
