package ga.abzzezz.serial;

import com.fazecast.jSerialComm.SerialPort;
import ga.abzzezz.rotation.RotationHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Class to handle the serial connection between App and port (COM4)
 */
public class SerialHandler {

    public static final SerialHandler INSTANCE = new SerialHandler();
    /**
     * End line char to indicate String done
     */
    final char endLine = '\n';
    /**
     * Separator char to split String
     */
    final String separator = ":";
    private SerialPort serialPort;
    private int index;

    /**
     * Set port
     *
     * @param newPort serial port in
     * @return is port opened?
     */
    public boolean setPort(final SerialPort newPort) {
        if(this.serialPort != null && this.serialPort.isOpen()) this.serialPort.closePort();

        this.serialPort = newPort;
        this.serialPort.setComPortParameters(9600, 8, 1, 0);
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (this.serialPort.openPort())
            Logger.getAnonymousLogger().log(Level.INFO, "Serial port\"" + this.serialPort.getDescriptivePortName() + "\" is now opened");
        else {
            Logger.getAnonymousLogger().log(Level.WARNING, "Serial port couldn't be opened");
            return false;
        }
        changeXAxis(RotationHandler.INSTANCE.getX());
        changeYAxis(RotationHandler.INSTANCE.getY());

        IntStream.range(0, SerialPort.getCommPorts().length).filter(value -> SerialPort.getCommPorts()[value].equals(this.serialPort)).findAny().ifPresent(value -> index = value);
        return true;
    }

    /**
     * Change the x-Servos rotation, if there's no difference, return.
     *
     * @param amount amount ton change to
     */
    public int changeXAxis(int amount) {
        if (amount > 180) amount = 180;
        if (!getSerialPort().isOpen())
            return RotationHandler.INSTANCE.getX();
        try {
            getSerialPort().getOutputStream().write(format('X', RotationHandler.INSTANCE.setX(amount)));
            getSerialPort().getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return amount;
    }

    /**
     * Change the y-Servos rotation, if there's no difference, return.
     *
     * @param amount amount ton change to
     */
    public int changeYAxis(int amount) {
        if (amount > 180) amount = 180;
        if (!getSerialPort().isOpen())
            return RotationHandler.INSTANCE.getY();
        try {
            getSerialPort().getOutputStream().write(format('Y', RotationHandler.INSTANCE.setY(amount)));
            getSerialPort().getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return amount;
    }

    /**
     * Format String to send based on @params
     *
     * @param mode   servo to control
     * @param amount new position to rotate to
     * @return byte array from formatted String
     */
    private byte[] format(final char mode, final int amount) {
        return (((int) mode) + separator + amount + endLine).getBytes();
    }

    /**
     * Current serial port, null if there is no port
     *
     * @return serial port
     */
    public SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * Port's index
     *
     * @return port index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set index
     *
     * @param index integer to set to
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
