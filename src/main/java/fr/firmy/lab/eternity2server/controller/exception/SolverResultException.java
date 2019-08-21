package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

import java.util.ArrayList;
import java.util.List;

public class SolverResultException extends AbstractMultipleErrorsException {

    public SolverResultException(List<ErrorDescription> errors) {
        super("The solver result cannot be integrated in database", errors);
    }
}
