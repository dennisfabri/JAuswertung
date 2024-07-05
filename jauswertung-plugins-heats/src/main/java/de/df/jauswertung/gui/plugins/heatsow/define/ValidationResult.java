package de.df.jauswertung.gui.plugins.heatsow.define;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Stream.concat;

public class ValidationResult {

    public static final ValidationResult OK = new ValidationResult(List.of());

    private final boolean valid;
    private final List<String> messages;

    public ValidationResult(List<String> messages) {
        valid = messages.isEmpty();
        this.messages = List.copyOf(messages);
    }

    public ValidationResult(String... messages) {
        valid = messages.length == 0;
        this.messages = Arrays.asList(messages);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return String.join("\n", messages);
    }

    public ValidationResult merge(ValidationResult validate) {
        if (valid) {
            return validate;
        }
        if (validate.valid) {
            return this;
        }
        return new ValidationResult(concat(messages.stream(), validate.messages.stream()).distinct().toList());
    }
}
