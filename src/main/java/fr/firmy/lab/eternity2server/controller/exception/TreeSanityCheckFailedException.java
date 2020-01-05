package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.model.dto.FailedCheckDescription;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

public class TreeSanityCheckFailedException extends AbstractMultipleErrorsException {

    public TreeSanityCheckFailedException(List<FailedCheckDescription> errors) {
        super(String.format("The Search Tree Sanity Check FAILED (for %d reasons)", errors.size()),
                errors.stream().map( failure -> new ErrorDescription( HttpStatus.INTERNAL_SERVER_ERROR, failure.getPath(), failure.getMessage() ) ).collect(Collectors.toList()));
    }

}
