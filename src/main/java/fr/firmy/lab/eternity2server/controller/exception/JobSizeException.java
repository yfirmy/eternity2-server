package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import org.springframework.http.HttpStatus;

public class JobSizeException extends AbstractSingleErrorException {

    public JobSizeException(int requestedSize, int boardSize) {
        super("The requested size must be between 0 and "+ boardSize,
                new ErrorDescription(HttpStatus.BAD_REQUEST, Integer.toString(requestedSize), "The requested size must be between 0 and "+ boardSize));
    }

}
