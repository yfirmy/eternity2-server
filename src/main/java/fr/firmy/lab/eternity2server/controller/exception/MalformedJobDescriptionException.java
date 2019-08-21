package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import org.springframework.http.HttpStatus;

public class MalformedJobDescriptionException extends AbstractSingleErrorException {

    public MalformedJobDescriptionException(String malformedJobDescription) {
        super("The given job description ("+malformedJobDescription+") is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedJobDescription, "The given job description is malformed"));
    }
}
