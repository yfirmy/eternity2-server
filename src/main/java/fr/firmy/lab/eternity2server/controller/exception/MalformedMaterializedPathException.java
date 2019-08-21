package fr.firmy.lab.eternity2server.controller.exception;

public class MalformedMaterializedPathException extends Exception {
    public MalformedMaterializedPathException(String malformedPath) {
        super("The given materialized path ("+malformedPath+") is malformed");
    }
}
