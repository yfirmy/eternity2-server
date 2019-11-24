package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import org.springframework.http.HttpStatus;

public class MalformedSolverDescriptionException extends AbstractSingleErrorException {

    public MalformedSolverDescriptionException(String malformedSolverDescription) {
        super("The given solver description is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedSolverDescription, "The given solver description is malformed"));
    }

    public MalformedSolverDescriptionException(String malformedSolverDescription, Throwable cause) {
        super("The given solver description is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedSolverDescription, "The given solver description is malformed"), cause);
    }
}
