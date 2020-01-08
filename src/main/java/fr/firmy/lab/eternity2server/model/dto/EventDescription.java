package fr.firmy.lab.eternity2server.model.dto;

public class EventDescription {

    private String solverName;
    private String status;

    public EventDescription() {
    }

    public EventDescription(String solverName, String status) {
        this.solverName = solverName;
        this.status = status;
    }

    public String getSolverName() {
        return solverName;
    }

    public void setSolverName(String solverName) {
        this.solverName = solverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
