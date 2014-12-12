package testmd;

import testmd.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

public abstract class OutputFormat {


    public static final OutputFormat DEFAULT = new OutputFormat.DefaultFormat();

    public abstract String format(Object value);

    private static class DefaultFormat extends OutputFormat {

        private final ArrayFormat ARRAY = new ArrayFormat(this);
        private final CollectionFormat COLLECTION = new CollectionFormat(this);

        @Override
        public String format(Object value) {
            if (value == null) {
                return null;
            }

            if (value instanceof Class) {
                return ((Class) value).getName();
            }

            if (value instanceof Object[]) {
                return ARRAY.format(value);
            }

            if (value instanceof Collection) {
                return COLLECTION.format(value);
            }

            if (value instanceof Value) {
                return ((Value) value).serialize();
            }

            return value.toString();
        }
    }

    public static class ArrayFormat extends OutputFormat {

        private StringUtils.JoinFormat joinFormat;

        public ArrayFormat(final OutputFormat joinFormat) {
            this.joinFormat = new StringUtils.JoinFormat() {
                @Override
                public String toString(Object obj) {
                    return joinFormat.format(obj);
                }
            };
        }

        @Override
        public String format(Object value) {
            if (value == null) {
                return null;
            }

            return StringUtils.join(Arrays.asList((Object[]) value), ", ", joinFormat, false);
        }
    }

    public static class CollectionFormat extends OutputFormat {

        private StringUtils.JoinFormat joinFormat;

        public CollectionFormat(final OutputFormat joinFormat) {
            this.joinFormat = new StringUtils.JoinFormat() {
                @Override
                public String toString(Object obj) {
                    return joinFormat.format(obj);
                }
            };
        }

        @Override
        public String format(Object value) {
            if (value == null) {
                return null;
            }
            return StringUtils.join(((Collection) value), ", ", joinFormat, false);
        }
    }
}
