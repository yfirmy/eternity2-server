package fr.firmy.lab.eternity2server.controller.exception;

import com.fasterxml.jackson.core.JsonParser;

public class BoardDescriptionParseException extends RuntimeException {
    public BoardDescriptionParseException(JsonParser p, String description, Throwable cause) {
        super("Impossible to deserialize a BoardDescription " + description, cause);
    }
}
