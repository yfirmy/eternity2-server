package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSingleErrorException extends Exception {

    private ErrorDescription error;

    public AbstractSingleErrorException(String message, ErrorDescription error, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public AbstractSingleErrorException(String message, ErrorDescription error) {
        super(message);
        this.error = error;
    }

    public ErrorDescription getError() {
        return this.error;
    }

}
