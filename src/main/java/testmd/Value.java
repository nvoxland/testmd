package testmd;

public class Value {
    private Object value;
    private OutputFormat format;
    private String stringValue;

    public Value(Object value, OutputFormat format) {
        this.value = value;
        this.format = format;
    }

    public Object getValue() {
        return value;
    }

    public String serialize() {
        if (stringValue == null) {
            stringValue = format.format(value);
        }
        return stringValue;
    }

    @Override
    public String toString() {
        return serialize();
    }
}
