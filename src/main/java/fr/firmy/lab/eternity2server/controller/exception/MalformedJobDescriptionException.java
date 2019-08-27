package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import org.springframework.http.HttpStatus;

public class MalformedJobDescriptionException extends AbstractSingleErrorException {

    public MalformedJobDescriptionException(JobDescription malformedJobDescription) {
        super("The given job description ("+malformedJobDescription.toString()+") is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedJobDescription.toString(), "The given job description is malformed"));
    }

    public MalformedJobDescriptionException(JobDescription malformedJobDescription, Throwable cause) {
        super("The given job description ("+malformedJobDescription.toString()+") is malformed",
                new ErrorDescription(HttpStatus.BAD_REQUEST, malformedJobDescription.toString(), "The given job description is malformed"), cause);
    }
}
