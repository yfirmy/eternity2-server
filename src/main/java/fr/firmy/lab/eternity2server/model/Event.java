package fr.firmy.lab.eternity2server.model;

public class Event {

    private String solverName;
    private Status status;

    public Event() {
    }

    public Event(String solverName, Status status) {
        this.solverName = solverName;
        this.status = status;
    }

    public String getSolverName() {
        return solverName;
    }

    public void setSolverName(String solverName) {
        this.solverName = solverName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        STARTED,
        REQUESTING,
        WAITING,
        SOLVING,
        REPORTING,
        STOPPED;

        public static Status parseStatus(String status) {

            switch (status) {
                case "STARTED" : return STARTED;
                case "REQUESTING" : return REQUESTING;
                case "WAITING" : return WAITING;
                case "SOLVING" : return SOLVING;
                case "REPORTING" : return REPORTING;
                case "STOPPED" : return STOPPED;
                default: throw new IllegalArgumentException("Unknown Status "+status);
            }
        }
    }
}
