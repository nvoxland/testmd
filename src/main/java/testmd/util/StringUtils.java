package testmd.util;

import testmd.Value;

import javax.xml.stream.util.StreamReaderDelegate;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.*;

/**
 * Various utility methods related to strings
 */
public class StringUtils {

    public static String computeKey(Map parameters) {
        if (parameters == null) {
            return null;
        }
        if (parameters.size() == 0) {
            return "";
        } else {
            List<String> list = new ArrayList<String>();

            JoinFormat format = StringUtils.STANDARD_STRING_FORMAT;
            SortedMap sortedMap = new TreeMap(format);
            sortedMap.putAll(parameters);

            for (Map.Entry entry : (Set<Map.Entry>) sortedMap.entrySet()) {
                String value = StringUtils.trimToNull(format.toString(entry.getValue()));
                if (value != null) {
                    list.add(format.toString(entry.getKey()) + "=" + value);
                }
            }

            return StringUtils.computeHash(join(list, ",", format, false));
        }
    }

    /**
     * Fully reads the given stream as a string. Closes the stream when finished, even if an exception occurs.
     */
    public static String read(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        InputStreamReader reader = new InputStreamReader(stream);
        StringBuilder result = new StringBuilder();

        char[] buffer = new char[2048];
        int read;
        try {
            while ((read = reader.read(buffer)) > -1) {
                result.append(buffer, 0, read);
            }
            return result.toString();
        } finally {
            stream.close();
        }
    }

    /**
     * Join objects in a collection. If delimiter is null, defaults to ",".
     */
    public static String join(Collection collection, String delimiter, JoinFormat format, boolean sorted) {
        if (collection == null) {
            return null;
        }

        if (collection.size() == 0) {
            return "";
        }

        if (delimiter == null) {
            delimiter = ",";
        }

        if (sorted) {
            TreeSet sortedSet = new TreeSet(format);
            for (Object obj : collection) {
                sortedSet.add(obj);
            }
            collection = sortedSet;
        }

        StringBuilder buffer = new StringBuilder();
        for (Object val : collection) {
            buffer.append(format.toString(val)).append(delimiter);
        }

        String returnString = buffer.toString();
        return returnString.substring(0, returnString.length() - delimiter.length());
    }

    public static String join(Collection<String> collection, String delimiter, boolean sorted) {
        return join(collection, delimiter, STANDARD_STRING_FORMAT, sorted);
    }

    public static String join(Map map, String delimiter, boolean sorted) {
        return join(map, delimiter, STANDARD_STRING_FORMAT, sorted);
    }

    /**
     * Joins entries in a map into a string using the format "KEY=VALUE".
     * If delimiter is null, defaults to ","
     * The passed JoinFormat is used for both the key and value formatting.
     */
    public static String join(Map map, String delimiter, JoinFormat format, boolean sorted) {
        List<String> list = new ArrayList<String>();
        if (sorted) {
            SortedMap sortedMap = new TreeMap(format);
            sortedMap.putAll(map);

            map = sortedMap;
        }
        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            list.add(format.toString(format.toString(entry.getKey()) + "=" + format.toString(entry.getValue())));
        }
        return join(list, delimiter, format, sorted);
    }

    /**
     * Returns given string trimmed. If null returns empty string.
     */
    public static String trimToEmpty(String string) {
        if (string == null) {
            return "";
        }
        return string.trim();
    }

    /**
     * Returns given string trimmed. If resulting string is empty, returns null.
     */
    public static String trimToNull(String string) {
        if (string == null) {
            return null;
        }
        String returnString = string.trim();
        if (returnString.length() == 0) {
            return null;
        } else {
            return returnString;
        }
    }

    /**
     * Returns given string value padded with spaces if its length is less than the minimumLength. If length is greater, the original value is returned.
     */
    public static String pad(String value, int minimumLength) {
        value = StringUtils.trimToEmpty(value);
        if (value.length() >= minimumLength) {
            return value;
        }

        return value + StringUtils.repeat(" ", minimumLength - value.length());
    }

    /**
     * Repeats the passed string the given number of times.
     */
    public static String repeat(String string, int times) {
        if (string == null) {
            return null;
        }
        String returnString = "";
        for (int i = 0; i < times; i++) {
            returnString += string;
        }

        return returnString;
    }

    /**
     * Indents the string with the given number of spaces
     */
    public static String indent(String string, int padding) {
        if (string == null) {
            return null;
        }
        String pad = StringUtils.repeat(" ", padding);
        return pad + (string.replaceAll("\n", "\n" + pad));
    }

    /**
     * Computes a short hash of the given string. Not secure, simply used to quickly identify a string.
     */
    public static String computeHash(String input) {
        if (input == null) {
            return "null";
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(input.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] digestBytes = digest.digest();

        return new String(encodeHex(digestBytes)).substring(0, 7);

    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     */
    public static char[] encodeHex(byte[] data) {
        char[] DIGITS_LOWER = {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }

        return out;
    }

    /**
     * Class used by the join methods for formatting objects as strings and/or sorting values in a collection.
     */
    public static abstract class JoinFormat<Type> implements Comparator<Type> {

        /**
         * Returns the string version of the given object.
         */
        public abstract String toString(Type obj);

        /**
         * Default compare method uses o1's comparable method if possible, otherwise falls back to comparing the result of {@link #toString(Object)} on the two objects.
         */
        @Override
        public int compare(Type o1, Type o2) {
            if (o1 instanceof Comparable && o2 instanceof Comparable && o1.getClass().isAssignableFrom(o2.getClass())) {
                return ((Comparable) o1).compareTo(o2);
            }
            return toString(o1).compareTo(toString(o2));
        }
    }

    /**
     * JoinFormatter that will nicely format maps, collections and arrays as joined, sorted lists. Other objects will use toString()
     */
    public static JoinFormat STANDARD_STRING_FORMAT = new JoinFormat() {
        @Override
        public String toString(Object obj) {
            if (obj == null) {
                return null;
            } else if (obj instanceof Object[]) {
                return "[" + StringUtils.join(Arrays.asList((Object[]) obj), ", ", this, true) + "]";
            } else if (obj instanceof Collection) {
                return "[" + StringUtils.join(((Collection) obj), ", ", this, true) + "]";
            } else if (obj instanceof Map) {
                return "[" + StringUtils.join(((Map) obj), ", ", this, true) + "]";
            } else if (obj instanceof Value) {
                return ((Value) obj).serialize();
            }
            return obj.toString();
        }

    };

}
