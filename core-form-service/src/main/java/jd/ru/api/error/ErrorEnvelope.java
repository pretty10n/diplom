package jd.ru.api.error;

import java.util.List;

public record ErrorEnvelope(
        ErrorBody error
) {
    public static ErrorEnvelope of(String code, String message, List<ErrorDetails> details) {
        return new ErrorEnvelope(new ErrorBody(code, message, details));
    }

    public record ErrorBody(
            String code,
            String message,
            List<ErrorDetails> details
    ) {
    }

    public record ErrorDetails(
            String field,
            String reason
    ) {
    }
}
