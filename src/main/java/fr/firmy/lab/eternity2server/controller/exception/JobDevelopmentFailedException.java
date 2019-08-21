package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

import java.util.List;

public class JobDevelopmentFailedException extends AbstractMultipleErrorsException {

    public JobDevelopmentFailedException(List<ErrorDescription> errors, Throwable cause) {
        super("Impossible to develop more jobs on the branch", errors, cause);
    }

}
