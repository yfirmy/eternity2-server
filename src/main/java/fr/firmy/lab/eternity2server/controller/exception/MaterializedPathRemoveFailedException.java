package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.MaterializedPath;

public class MaterializedPathRemoveFailedException extends Exception {

    public MaterializedPathRemoveFailedException(MaterializedPath materializedPath) {
        super("Impossible to remove the given Materialized Path ("+materializedPath+")");
    }

}
