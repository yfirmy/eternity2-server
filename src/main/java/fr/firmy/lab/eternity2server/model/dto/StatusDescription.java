package fr.firmy.lab.eternity2server.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.firmy.lab.eternity2server.model.dto.serializer.StatusDescriptionDeserializer;

import java.util.Date;

@JsonDeserialize(using = StatusDescriptionDeserializer.class)
public class StatusDescription {

    private JobDescription job;
    private String status;
    private Date dateJobTransmission;
    private Date dateStatusUpdate;
    private SolverDescription solverDescription;

    public StatusDescription() {
    }

    public StatusDescription(JobDescription initialJob, String status, Date dateJobTransmission, Date dateStatusUpdate, SolverDescription solverDescription) {
        this.job = initialJob;
        this.status = status;
        this.dateJobTransmission = dateJobTransmission;
        this.dateStatusUpdate = dateStatusUpdate;
        this.solverDescription = solverDescription;
    }

    public JobDescription getJob() {
        return job;
    }

    public void setJob(JobDescription job) {
        this.job = job;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateJobTransmission() {
        return dateJobTransmission;
    }

    public void setDateJobTransmission(Date dateJobTransmission) {
        this.dateJobTransmission = dateJobTransmission;
    }

    public Date getDateStatusUpdate() {
        return dateStatusUpdate;
    }

    public void setDateStatusUpdate(Date dateStatusUpdate) {
        this.dateStatusUpdate = dateStatusUpdate;
    }

    public SolverDescription getSolverDescription() {
        return solverDescription;
    }

    public void setSolverDescription(SolverDescription solverDescription) {
        this.solverDescription = solverDescription;
    }
}
