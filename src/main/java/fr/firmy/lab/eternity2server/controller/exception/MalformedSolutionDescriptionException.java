package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.model.dto.SolutionDescription;
import org.springframework.http.HttpStatus;

public class MalformedSolutionDescriptionException extends AbstractSingleErrorException {

    public MalformedSolutionDescriptionException(SolutionDescription malformedSolutionDescription) {
        super("The given solution description ("+malformedSolutionDescription.toString()+") is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedSolutionDescription.toString(), "The given solution description is malformed"));
    }

    public MalformedSolutionDescriptionException(SolutionDescription malformedSolutionDescription, Throwable cause) {
        super("The given solution description ("+malformedSolutionDescription.toString()+") is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedSolutionDescription.toString(), "The given solution description is malformed"), cause);
    }
}
