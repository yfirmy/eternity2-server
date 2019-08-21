package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultipleErrorsException extends Exception {

    private final List<ErrorDescription> errors = new ArrayList<>();

    public AbstractMultipleErrorsException(String message, List<ErrorDescription> errors, Throwable cause) {
        super(message, cause);
        this.errors.addAll(errors);
    }

    public AbstractMultipleErrorsException(String message, List<ErrorDescription> errors) {
        super(message);
        this.errors.addAll(errors);
    }

    public List<ErrorDescription> getErrors() {
        return this.errors;
    }

}
