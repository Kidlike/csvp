package kidlike.csvp.polyglot;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.polyglot.Value;

public class PolyglotUtils {
    private PolyglotUtils() {
        // utility class
    }

    /**
     * Convert a polyglot array {@link Value} to a java list.
     *
     * @param value {@link Value#hasArrayElements() must have array elements}
     * @return a java list where all polyglot elements have been stringified.
     */
    public static List<String> toList(Value value) {
        if (!value.hasArrayElements()) {
            throw new IllegalArgumentException("Expected array, but got: " + value.getMetaSimpleName());
        }

        List<String> ret = new ArrayList<>((int) value.getArraySize());

        for (int i = 0; i < value.getArraySize(); i++) {
            ret.add(value.getArrayElement(i).toString());
        }

        return ret;
    }
}
