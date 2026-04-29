package com.pharmatrack.elogbook.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) { super(HttpStatus.FORBIDDEN, "FORBIDDEN", message); }
}
