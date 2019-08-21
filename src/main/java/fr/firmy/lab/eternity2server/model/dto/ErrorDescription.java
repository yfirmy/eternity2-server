package fr.firmy.lab.eternity2server.model.dto;

import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.Map;

public class ErrorDescription {

    private int status;
    private String error;
    private String message;
    private String path;
    private Date timestamp;
    private String requestURI;
    private Map<String, String[]> parameters;

    public ErrorDescription() {
    }

    public ErrorDescription(HttpStatus status, String path, String message) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
        this.timestamp = new Date();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }
}
