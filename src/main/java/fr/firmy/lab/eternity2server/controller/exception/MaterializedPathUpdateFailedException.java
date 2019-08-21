package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;

public class MaterializedPathUpdateFailedException extends AbstractSingleErrorException {

    public MaterializedPathUpdateFailedException(MaterializedPath materializedPath, ErrorDescription error, Throwable cause) {
        super("Impossible to update the given Materialized Path ("+materializedPath+")", error, cause);
    }

    public MaterializedPathUpdateFailedException(MaterializedPath materializedPath, ErrorDescription error) {
        super("Impossible to update the given Materialized Path ("+materializedPath+")", error);
    }

}
