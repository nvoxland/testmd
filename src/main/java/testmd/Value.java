package testmd;

/**
 * Container storing an object value plus an {@link ValueFormat} to control how the object is {@link #serialize()}-ed when saved with {@link testmd.storage.ResultsWriter}
 */
public class Value {
    private Object value;
    private ValueFormat format;

    /**
     * Constructs new Value object. If format is null, defaults to {@link ValueFormat.DefaultFormat}
     */
    public Value(Object value, ValueFormat format) {
        this.value = value;
        this.format = format;
        if (this.format == null) {
            this.format = ValueFormat.DEFAULT;
        }
    }

    /**
     * Returns the raw object stored in this Value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the value stored in this object, formatted by the assigned {@link @OutputFormat}
     */
    public String serialize() {
        return format.format(value);
    }

    /**
     * Calls {@link #serialize()}
     */
    @Override
    public String toString() {
        return serialize();
    }
}
