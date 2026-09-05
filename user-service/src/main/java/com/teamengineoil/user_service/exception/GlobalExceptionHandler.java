package com.teamengineoil.user_service.exception;

import com.teamengineoil.user_service.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(HomeEnergyTrackerException.class)
    public ResponseEntity<ErrorResponse> handleHomeEnergyTrackerException(
            HomeEnergyTrackerException ex
    ) {

        ErrorCode errorCode = ex.getErrorCode();

        String message = messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                LocaleContextHolder.getLocale()
        );

        ErrorResponse response = new ErrorResponse(
                errorCode.getCode(),
                message
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex
    ) {
        log.error("Generic Exception Occurred: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(
                "error.internal",
                null,
                LocaleContextHolder.getLocale()
        );

        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                message
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}