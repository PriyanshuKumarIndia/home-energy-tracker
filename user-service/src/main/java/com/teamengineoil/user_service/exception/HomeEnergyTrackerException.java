package com.teamengineoil.user_service.exception;

import lombok.Getter;

@Getter
public class HomeEnergyTrackerException extends RuntimeException {

    private final ErrorCode errorCode;

    public HomeEnergyTrackerException(ErrorCode errorCode) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
    }

    public HomeEnergyTrackerException(
            ErrorCode errorCode,
            Throwable cause
    ) {
        super(errorCode.getMessageKey(), cause);
        this.errorCode = errorCode;
    }
}