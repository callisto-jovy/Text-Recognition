/*
 * Created by Roman P.  (2020)
 *
 *
 *
 *
 */

package ga.abzzezz.util;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple data saving api to use in my projects.
 * Nothing compared to libraries such as JSON, GSON etc.
 */

public class SaveObject {

    private static final String BLOCK_START = "{";
    private static final String SEPARATOR = "\":\"";
    private static final String BLOCK_END = "\"}";
    private final CharFormatter charFormatter = new CharFormatter(new HashMap<String, String>() {
        {
            {
                put("\"", "\\\"\\");
                put("\n", "\\n");
                put("\r", "\\r");
                put("\t", "\\t");
                put("\b", "\\b");
            }
        }
    });
    private final Map<String, Object> stringValueHashtable = new HashMap<String, Object>() {
        @Override
        public Object get(Object key) {
            return charFormatter.rebuild(super.get(charFormatter.format(key.toString())).toString());
        }
    };

    public SaveObject(final String string) {
        this.load(string);
    }

    public SaveObject() {
    }

    public SaveObject putInt(final String key, final int value) {
        put(key, value);
        return this;
    }

    public SaveObject putString(final String key, final String value) {
        put(key, value);
        return this;
    }

    public SaveObject put(final String key, final SaveObject saveObject) {
        putString(key, saveObject.toString());
        return this;
    }

    public SaveObject putDouble(final String key, final double value) {
        put(key, value);
        return this;
    }

    public SaveObject putFloat(final String key, final float value) {
        put(key, value);
        return this;
    }

    public SaveObject putLong(final String key, final long value) {
        put(key, value);
        return this;
    }

    public SaveObject putByte(final String key, final byte value) {
        put(key, value);
        return this;
    }

    public SaveObject putChar(final String key, final char value) {
        //Generally same as put int.
        putInt(key, value);
        return this;
    }

    public SaveObject putCollection(final String key, final Collection<?> list) {
        put(key, list.stream().map(Object::toString).collect(Collectors.joining(",")));
        return this;
    }

    public <E> SaveObject putArray(final String key, final E[] array) {
        put(key, Arrays.stream(array).map(Object::toString).collect(Collectors.joining(",")));
        return this;
    }

    public SaveObject putMap(final String key, final Map<?, ?> map) {
        put(key, map.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(",")));
        return this;
    }

    public SaveObject putShort(final String key, final short value) {
        put(key, value);
        return this;
    }

    public SaveObject putBoolean(final String key, final boolean value) {
        put(key, value);
        return this;
    }

    public SaveObject put(final String key, final Object value) {
        stringValueHashtable.put(charFormatter.format(key), charFormatter.format(value.toString()));
        return this;
    }

    public Object get(final String key) {
        return stringValueHashtable.get(key);
    }

    public int getInt(final String key) {
        return Integer.parseInt(getString(key));
    }

    public String getString(final String key) {
        return get(key).toString();
    }

    public char getChar(final String key) {
        return (char) get(key);
    }

    public short getShort(final String key) {
        return Short.parseShort(getString(key));
    }

    public byte getByte(final String key) {
        return Byte.parseByte(getString(key));
    }

    public boolean getBoolean(final String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public float getFloat(final String key) {
        return Float.parseFloat(getString(key));
    }

    public double getDouble(final String key) {
        return Double.parseDouble(getString(key));
    }

    public long getLong(final String key) {
        return Long.parseLong(getString(key));
    }

    public Collection<?> getList(final String key) {
        return Arrays.asList(getArray(key));
    }

    public String[] getArray(final String key) {
        try {
            return getString(key).split(",");
        } catch (IllegalArgumentException e) {
            new SaveObjectException("Collecting array").printStackTrace();
            return new String[0];
        }
    }

    public Map<?, ?> getMap(final String key) {
        final Map map = new HashMap<>();
        try {
            final String value = getString(key);
            final String[] entries = value.split(",");
            for (String entry : entries) {
                final String[] keyAndValue = entry.split("=");
                map.put(keyAndValue[0], keyAndValue[1]);
            }
        } catch (RuntimeException e) {
            new SaveObjectException("Collecting hashmap").printStackTrace();
        }
        return map;
    }

    private String decode(final String string) {
        try {
            return URLDecoder.decode(string, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            new SaveObjectException("Decoding charset not supported").printStackTrace();
        }
        return "";
    }

    public String toString() {
        return stringValueHashtable.entrySet().stream().map(this::format).collect(Collectors.joining());
    }

    private String format(final Map.Entry<String, Object> entry) {
        final String s = decode(entry.getKey()).concat(SEPARATOR).concat(decode(entry.getValue().toString())).concat(BLOCK_END);
        return BLOCK_START.concat(s.length() + "\"" + s);
    }

    public String get(final File file) throws FileNotFoundException {
        return new BufferedReader(new FileReader(file)).lines().collect(Collectors.joining());
    }

    public void save(final File file, final boolean append) {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(file, append);
            fileOutputStream.write(toString().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            new SaveObjectException("Writing to file").printStackTrace();
        }
    }

    private void load(final String string) {
        final StringBuilder blocks = new StringBuilder(string);
        while (blocks.length() > 0) {
            try {
                //Find next start
                final int startBlock = blocks.indexOf(BLOCK_START);
                //Find the start of the key, also to calculate the blocks length
                final int keyStart = blocks.indexOf("\"", startBlock);
                //Get the index of the colon, to separate key and value
                final int indexColon = blocks.indexOf(SEPARATOR, keyStart);
                //Calculate length
                final int blockLength = Integer.parseInt(blocks.substring(startBlock + 1, keyStart));
                //Get end, based on blocks length
                final int endBlock = blocks.indexOf(BLOCK_END, startBlock + blockLength);
                //Get key and value, also replace quotation marks
                final String key = charFormatter.rebuild(decode(blocks.substring(keyStart + 1, indexColon)));
                final String value = decode(blocks.substring(indexColon + SEPARATOR.length(), endBlock));
                //Put
                this.stringValueHashtable.put(key, value);
                //Delete from blocks list
                blocks.delete(startBlock, endBlock + BLOCK_END.length());
            } catch (RuntimeException e) {
                new SaveObjectException("The loaded in data string is damaged: " + e.getMessage()).printStackTrace();
            }

        }
    }

    public Map<String, Object> getAll() {
        return stringValueHashtable;
    }

    /**
     * Class to replace certain characters with a counterpart and rereplace them
     * Takes in either two array (have to be same length & order to work obv.)
     * or a hashmap
     */
    public static class CharFormatter {

        private final Map<String, String> keyReplace = new HashMap<>();

        /**
         * Takes in two similar arrays
         *
         * @param disallowed  chars to be replaced
         * @param replacement replacement
         */
        public CharFormatter(final String[] disallowed, final String[] replacement) {
            for (int i = 0; i < disallowed.length; i++) {
                keyReplace.put(disallowed[i], replacement[i]);
            }
        }

        public CharFormatter(HashMap<String, String> stringStringHashMap) {
            keyReplace.putAll(stringStringHashMap);
        }

        public String format(final String string) {
            return string.chars().parallel().mapToObj(value -> String.valueOf((char) value)).map(value -> keyReplace.getOrDefault(value, value)).collect(Collectors.joining());
        }

        public String rebuild(String string) {
            for (Map.Entry<String, String> stringStringEntry : keyReplace.entrySet()) {
                string = string.replace(stringStringEntry.getValue(), stringStringEntry.getKey());
            }
            return string;
        }
    }

    /**
     * Simple throwable to display errors when working with this api & give reasoning
     */
    public static class SaveObjectException extends Throwable {

        public final String reason;

        public SaveObjectException(String reason) {
            this.reason = reason;
        }

        @Override
        public String getMessage() {
            return "Exception thrown while operating on data: " + reason;
        }

        @Override
        public void printStackTrace(PrintStream s) {
            super.printStackTrace(s);
        }
    }
}
