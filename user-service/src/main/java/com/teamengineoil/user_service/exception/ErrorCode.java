package com.teamengineoil.user_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(
            "USER_NOT_FOUND",
            "user.not.found",
            HttpStatus.NOT_FOUND
    ),

    USER_ALREADY_EXISTS(
            "USER_ALREADY_EXISTS",
            "user.already.exists",
            HttpStatus.CONFLICT
    ),

    INVALID_CREDENTIALS(
            "INVALID_CREDENTIALS",
            "auth.invalid.credentials",
            HttpStatus.UNAUTHORIZED
    ),

    RESOURCE_NOT_FOUND(
            "RESOURCE_NOT_FOUND",
            "error.resource.not.found",
            HttpStatus.NOT_FOUND
    ),

    INVALID_REQUEST(
            "INVALID_REQUEST",
            "error.invalid.request",
            HttpStatus.BAD_REQUEST
    ),

    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            "error.internal",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String messageKey;
    private final HttpStatus status;

    ErrorCode(
            String code,
            String messageKey,
            HttpStatus status
    ) {
        this.code = code;
        this.messageKey = messageKey;
        this.status = status;
    }
}