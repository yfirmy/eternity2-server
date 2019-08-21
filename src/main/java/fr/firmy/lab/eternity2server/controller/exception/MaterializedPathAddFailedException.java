package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.MaterializedPath;

public class MaterializedPathAddFailedException extends Exception {

    public MaterializedPathAddFailedException(MaterializedPath materializedPath) {
        super("Impossible to add the given Materialized Path ("+materializedPath+")");
    }

}
