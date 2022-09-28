package com.krock.xmlapp.exception;

import org.springframework.web.server.ResponseStatusException;

public class GlobalException extends ResponseStatusException {

    public GlobalException(int rawStatusCode, String reason, Throwable cause) {
        super(rawStatusCode, reason, cause);
    }
}
