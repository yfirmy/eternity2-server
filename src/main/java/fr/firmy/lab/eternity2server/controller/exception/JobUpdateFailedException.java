package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

public class JobUpdateFailedException extends AbstractSingleErrorException {

    public JobUpdateFailedException(ErrorDescription error) {
        super("Impossible to update the given job", error);
    }

    public JobUpdateFailedException(ErrorDescription error, Throwable cause) {
        super("Impossible to update the given job", error, cause);
    }

}
