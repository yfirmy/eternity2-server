package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.model.dto.EventDescription;
import org.springframework.http.HttpStatus;

public class MalformedEventDescriptionException extends AbstractSingleErrorException {

    public MalformedEventDescriptionException(EventDescription malformedEventDescription, Throwable cause) {
        super("The given event description ("+malformedEventDescription.toString()+") is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedEventDescription.toString(), "The given event description is malformed"), cause);
    }
}
