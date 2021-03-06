package fr.firmy.lab.eternity2server.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.firmy.lab.eternity2server.model.dto.serializer.ResultDescriptionDeserializer;

import java.util.Date;
import java.util.List;

@JsonDeserialize(using= ResultDescriptionDeserializer.class)
public class ResultDescription {

    private JobDescription job;
    private List<SolutionDescription> solutions; // resulting boards
    private Date dateJobTransmission;
    private SolverDescription solverDescription;

    public ResultDescription() {
    }

    public ResultDescription(JobDescription job, List<SolutionDescription> solutions, Date dateCreated, SolverDescription solverDescription) {
        this.job = job;
        this.solutions = solutions;
        this.dateJobTransmission = dateCreated;
        this.solverDescription = solverDescription;
    }

    public JobDescription getJob() {
        return this.job;
    }

    public void setJob(JobDescription job) {
        this.job = job;
    }

    public List<SolutionDescription> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<SolutionDescription> solutions) {
        this.solutions = solutions;
    }

    public Date getDateJobTransmission() {
        return dateJobTransmission;
    }

    public void setDateJobTransmission(Date dateJobTransmission) {
        this.dateJobTransmission = dateJobTransmission;
    }

    public SolverDescription getSolverDescription() {
        return solverDescription;
    }

    public void setSolverDescription(SolverDescription solverDescription) {
        this.solverDescription = solverDescription;
    }

}
