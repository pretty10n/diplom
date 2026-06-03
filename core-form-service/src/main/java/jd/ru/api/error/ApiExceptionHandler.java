package jd.ru.api.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorEnvelope> handleApiException(ApiException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        List<ErrorEnvelope.ErrorDetails> details = ex.getDetails()
                .stream()
                .map(d -> new ErrorEnvelope.ErrorDetails(d.field(), d.reason()))
                .toList();
        return ResponseEntity.status(status)
                .body(ErrorEnvelope.of(ex.getCode(), ex.getMessage(), details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorEnvelope.ErrorDetails> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetails)
                .toList();
        String summary = details.stream()
                .map(d -> ((d.field() == null || d.field().isBlank()) ? "" : d.field() + ": ") + d.reason())
                .collect(Collectors.joining("; "));
        if (summary.isBlank()) {
            summary = "Validation failed";
        }
        return ResponseEntity.badRequest()
                .body(ErrorEnvelope.of("VALIDATION_ERROR", summary, details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorEnvelope> handleUnhandled(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorEnvelope.of("INTERNAL_ERROR", "Unexpected internal error", List.of()));
    }

    private ErrorEnvelope.ErrorDetails toDetails(FieldError fieldError) {
        return new ErrorEnvelope.ErrorDetails(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
