package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

import java.util.ArrayList;
import java.util.List;

public class MaterializedPathReplaceFailedException extends Exception {

    private final List<ErrorDescription> errors = new ArrayList<>();

    public MaterializedPathReplaceFailedException(List<ErrorDescription> errors) {
        super("Impossible to replace some of the given Materialized Paths");
        this.errors.addAll( errors );
    }

    public List<ErrorDescription> getErrors() { return errors; }
}
