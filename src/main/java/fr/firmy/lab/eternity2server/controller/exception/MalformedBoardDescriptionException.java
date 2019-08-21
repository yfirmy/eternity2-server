package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import org.springframework.http.HttpStatus;

public class MalformedBoardDescriptionException extends AbstractSingleErrorException {

    public MalformedBoardDescriptionException(String malformedBoardDescription) {
        super("The given board description ("+malformedBoardDescription+") is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedBoardDescription, "The given board description is malformed"));
    }
}
