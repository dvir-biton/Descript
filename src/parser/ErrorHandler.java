package parser;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    final List<String> errors = new ArrayList<>();

    public void logError(String error, int line) {
        errors.add(error.concat(" at: " + line));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void printErrors() {
        for (String error : errors) {
            System.out.println(error);
        }
    }
}
