package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import org.springframework.http.HttpStatus;

public class JobRetrievalFailedException extends AbstractSingleErrorException {

    public JobRetrievalFailedException(int jobSize, Throwable cause) {
        super("Impossible to retrieve the requested jobs",
                new ErrorDescription(HttpStatus.INTERNAL_SERVER_ERROR, Integer.toString(jobSize), "The requested job retrieval failed for jobSize "+ jobSize), cause);
    }

}
