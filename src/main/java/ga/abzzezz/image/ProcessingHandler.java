package ga.abzzezz.image;

import ga.abzzezz.Main;
import ga.abzzezz.util.FileUtil;
import ga.abzzezz.util.SettingsHolder;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
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
    private final Mat videoMat = new Mat();
    /**
     * Service for video thread
     **/
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    /**
     * Custom thresholds
     */
    private final double[] thresholds = new double[2];

    public ProcessingHandler() {
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("ssd");
        tesseract.setTessVariable("user_defined_dpi", "300");
    }

    /**
     * Start the video capture, run thread and process image.
     *
     * @param imageView
     * @param foundText
     */
    public void start(final ImageView imageView, final ListView<String> foundText) {
        videoCapture.open(1);
        executorService.submit(new Thread(() -> {
            while (videoCapture.isOpened() && videoCapture.read(videoMat)) {
                final MatOfByte processedByteMat = new MatOfByte();
                final Mat grayMat = new Mat();
                Imgproc.cvtColor(videoMat, grayMat, Imgproc.COLOR_BGR2GRAY, 0);
                Imgproc.Canny(grayMat, grayMat, getThresholds()[0], getThresholds()[1]);
                //    Imgproc.blur(grayMat, grayMat, new Size(5, 5));
                Imgcodecs.imencode(".jpg", grayMat, processedByteMat);
                final byte[] bytes = processedByteMat.toArray();
                imageView.setImage(new Image(new ByteArrayInputStream(bytes)));

                foundText.getItems().add(doOCR(grayMat));

                processedByteMat.release();
                grayMat.release();
                videoMat.release();
            }
        }));
    }

    public String doOCR(final Mat grayMat) {
        final BufferedImage image = new BufferedImage(grayMat.width(), grayMat.height(), BufferedImage.TYPE_BYTE_GRAY);
        final byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        grayMat.get(0, 0, data);
        try {
            final String tessGuess = tesseract.doOCR(image);
            if (!tessGuess.isEmpty()) {
                if (SettingsHolder.logResultsToFile)
                    FileUtil.writeStringToFile(Main.INSTANCE.getProcessedFile(), tessGuess, true);
                return tessGuess;
            }
        } catch (final TesseractException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    public void stop() {
        if (videoCapture.isOpened())
            videoCapture.release();
        try {
            if (executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdown();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        videoMat.release();
    }

    public Tesseract getTesseract() {
        return tesseract;
    }

    public VideoCapture getVideoCapture() {
        return videoCapture;
    }

    public double[] getThresholds() {
        return thresholds;
    }

    private double clamp(final double value, final double min, final double max) {
        return value > max ? max : Math.max(value, min);
    }

    public double setThreshold1(double value) {
        value = clamp(value, 0, 255);
        getThresholds()[0] = value;
        return value;
    }

    public double setThreshold2(double value) {
        value = clamp(value, 0, 255);
        getThresholds()[1] = value;
        return value;
    }
}
