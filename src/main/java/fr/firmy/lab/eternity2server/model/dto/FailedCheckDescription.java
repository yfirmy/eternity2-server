package fr.firmy.lab.eternity2server.model.dto;

public class FailedCheckDescription {

    private String message;
    private String path;

    public FailedCheckDescription() {
    }

    public FailedCheckDescription(String message, String path) {
        this.message = message;
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
