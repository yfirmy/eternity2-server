package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

import java.util.List;

public class JobPruneFailedException extends AbstractMultipleErrorsException {

    public JobPruneFailedException(List<ErrorDescription> errors, Throwable cause) {
        super("Impossible to prune the given job", errors, cause);
    }

}
