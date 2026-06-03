package jd.ru.api.error;

import java.util.List;

public class ApiException extends RuntimeException {
    private final String code;
    private final List<Detail> details;

    public ApiException(String code, String message) {
        this(code, message, List.of());
    }

    public ApiException(String code, String message, List<Detail> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public record Detail(
            String field,
            String reason
    ) {
    }
}
