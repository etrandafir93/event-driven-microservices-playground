package io.github.etr.playground.infra;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
class RestErrorHandler {

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> handleGenericException(Exception err) {
        var httpStatus = switch (err) {
            case NoSuchElementException __ -> HttpStatus.NOT_FOUND;
            case IllegalArgumentException __ -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return responseEntity(err, httpStatus);
    }

    private static ResponseEntity<Map<String, Object>> responseEntity(Exception error, HttpStatus status) {
        log.error("An error occurred while processing the order", error);
        return ResponseEntity.status(status)
            .body(Map.of("error", error.getMessage(), "status", status.value()));
    }
}
