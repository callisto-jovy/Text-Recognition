/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import ga.abzzezz.Main;
import ga.abzzezz.util.MathUtil;
import ga.abzzezz.util.QuickLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
            public void serialEvent(final SerialPortEvent serialPortEvent) {
                QuickLog.log("Received data from serial port: " + new String(serialPortEvent.getReceivedData(), StandardCharsets.UTF_8), QuickLog.LogType.INFO);
            }
        });

        if (this.serialPort.openPort())
            QuickLog.log("Serial port\"" + this.serialPort.getDescriptivePortName() + "\" is now opened", QuickLog.LogType.INFO);
        else {
            QuickLog.log("Serial port couldn't be opened", QuickLog.LogType.WARNING);
            return false;
        }

        for (int i = 0; i < SerialPort.getCommPorts().length; i++) {
            if (SerialPort.getCommPorts()[i].getDescriptivePortName().equals(serialPort.getDescriptivePortName())) {
                setIndex(i);
            }
        }

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
        if (!getSerialPort().isPresent()) return Main.INSTANCE.getRotationHandler().getX();
        if (!serialPort.isOpen())
            return Main.INSTANCE.getRotationHandler().getX();

        final int a = changeAmount(amount, 'X');
        Main.INSTANCE.getRotationHandler().setX(a);
        return a;
    }

    /**
     * Change a certain axis by a amount
     *
     * @param amount amount to change axis by
     * @param mode   axis
     * @return amount (clamped if needed)
     */
    private int changeAmount(int amount, final char mode) {
        amount = (int) MathUtil.clamp(amount, 45, 180);
        try {
            serialPort.getOutputStream().write(format(mode, amount));
            serialPort.getOutputStream().flush();
        } catch (final IOException e) {
            QuickLog.log("Writing to port", QuickLog.LogType.ERROR);
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
        if (!getSerialPort().isPresent()) return Main.INSTANCE.getRotationHandler().getX();
        if (!serialPort.isOpen())
            return Main.INSTANCE.getRotationHandler().getY();
        final int a = changeAmount(amount, 'Y');
        Main.INSTANCE.getRotationHandler().setY(a);
        return a;
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
    public Optional<SerialPort> getSerialPort() {
        return Optional.ofNullable(serialPort);
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
