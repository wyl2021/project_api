package com.example.project.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private List<String> errors;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}