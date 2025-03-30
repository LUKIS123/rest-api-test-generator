package pl.edu.pwr.compiler;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;

import java.util.List;

public class Helper {
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str); // Checks for both int and float
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean(String str) {
        if (str == null) {
            return false;
        }
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
    }

    public static String getValidJsonValue(String value) {
        String valueTrimmed = value.substring(1, value.length() - 1);
        if (Helper.isNumeric(valueTrimmed) || Helper.isBoolean(valueTrimmed)) {
            return valueTrimmed;
        } else {
            return "\"" + valueTrimmed + "\"";
        }
    }
}
