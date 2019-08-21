package fr.firmy.lab.eternity2server.model.dto;

public class JobDescription {

    private BoardDescription job;

    public JobDescription() {
    }

    public JobDescription(BoardDescription description) {
        this.job = description;
    }

    public BoardDescription getJob() {
        return this.job;
    }

    public void setJob(BoardDescription job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return this.job.toString();
    }

}
