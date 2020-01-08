package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

public class PostEventFailedException extends AbstractSingleErrorException {

    public PostEventFailedException(ErrorDescription error) {
        super("Impossible to post the given event", error);
    }

    public PostEventFailedException(ErrorDescription error, Throwable cause) {
        super("Impossible to post the given event", error, cause);
    }

}
