package net.nightwhistler.htmlspanner.utils;

/**
 * Created by adrianbudzynski on 09.06.2017.
 */

public class ParseUtils {

    public static int parseIntegerSafe(String value, int defaultValue) {
        try {
            if(value == null) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

}
