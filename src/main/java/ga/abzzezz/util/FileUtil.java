package ga.abzzezz.util;

import java.io.*;
import java.nio.channels.Channels;
import java.util.stream.Collectors;

public class FileUtil {
    /**
     * Reads all lines and collects them into a single String
     *
     * @param fileIn file to read from
     * @return String collected from all lines
     */
    public static String getFileContentsAsString(final File fileIn) {
        try {
            return new BufferedReader(new FileReader(fileIn)).lines().collect(Collectors.joining());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * Reads all lines and collects them into a single String with a delimiter
     *
     * @param fileIn    file to read from
     * @param delimiter delimiter
     * @return String collected from all lines
     */
    public static String getFileContentsAsString(final File fileIn, final String delimiter) {
        try {
            return new BufferedReader(new FileReader(fileIn)).lines().collect(Collectors.joining(delimiter));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Write a string to a given file. If the file does not exist it is created
     *
     * @param fileOut file to write to
     * @param string  string to write
     * @param append  should string be appended
     */
    public static void writeStringToFile(final File fileOut, final String string, final boolean append) {
        if (!fileOut.exists()) {
            try {
                fileOut.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileOut, append))) {
            bufferedWriter.write(string);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Outputs bytes to a given file
     *
     * @param fileOut file to write to
     * @param bytes   to write
     * @param append  should bytes be appended
     */
    public static void writeBytesToFile(final File fileOut, final byte[] bytes, final boolean append) {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(fileOut, append)) {
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read bytes from file
     *
     * @param fileIn file to read from
     * @return bytes
     */
    public static byte[] readFromFile(final File fileIn) {
        try (final FileInputStream fileInputStream = new FileInputStream(fileIn)) {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            fileInputStream.getChannel().transferTo(0, fileIn.length(), Channels.newChannel(byteArrayOutputStream));
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
