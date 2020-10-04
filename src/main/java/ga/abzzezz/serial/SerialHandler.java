package ga.abzzezz.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import ga.abzzezz.Main;
import ga.abzzezz.rotation.RotationHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to handle the serial connection between App and port (COM4)
 */
public class SerialHandler {
    /**
     * End line char to indicate String done
     */
    final char endLine = '\n';
    /**
     * Separator char to split String
     */
    final String separator = ":";

    private int index = -1;

    private SerialPort serialPort;

    /**
     * Set port
     *
     * @param newPort serial port in
     * @return is port opened?
     */
    public boolean setPort(final SerialPort newPort) {
        if (this.serialPort != null && this.serialPort.isOpen()) this.serialPort.closePort();

        this.serialPort = newPort;
        this.serialPort.setComPortParameters(9600, 8, 1, 0);
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        this.serialPort.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() {
                return "\n".getBytes();
            }

            @Override
            public boolean delimiterIndicatesEndOfMessage() {
                return true;
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                System.out.println(new String(serialPortEvent.getReceivedData()));
            }
        });

        if (this.serialPort.openPort())
            Logger.getAnonymousLogger().log(Level.INFO, "Serial port\"" + this.serialPort.getDescriptivePortName() + "\" is now opened");
        else {
            Logger.getAnonymousLogger().log(Level.WARNING, "Serial port couldn't be opened");
            return false;
        }
        // IntStream.range(0, SerialPort.getCommPorts().length).filter(value -> SerialPort.getCommPorts()[value].equals(this.serialPort)).findAny().ifPresent(value -> index = value);
        changeXAxis(Main.INSTANCE.getRotationHandler().getX());
        changeYAxis(Main.INSTANCE.getRotationHandler().getY());
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
            return Main.INSTANCE.getRotationHandler().getX();
        try {
            getSerialPort().getOutputStream().write(format('X', Main.INSTANCE.getRotationHandler().setX(amount)));
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
            return Main.INSTANCE.getRotationHandler().getY();
        try {
            getSerialPort().getOutputStream().write(format('Y', Main.INSTANCE.getRotationHandler().setY(amount)));
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

    public void setIndex(final int index) {
        this.index = index;
    }
}
