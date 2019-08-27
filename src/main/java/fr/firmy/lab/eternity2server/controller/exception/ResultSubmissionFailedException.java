package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

import java.util.List;

public class ResultSubmissionFailedException extends AbstractMultipleErrorsException {

    public ResultSubmissionFailedException(List<ErrorDescription> errors) {
        super("Impossible to submit the given results", errors);
    }

    public ResultSubmissionFailedException(List<ErrorDescription> errors, Throwable cause) {
        super("Impossible to submit the given results", errors, cause);
    }

}
