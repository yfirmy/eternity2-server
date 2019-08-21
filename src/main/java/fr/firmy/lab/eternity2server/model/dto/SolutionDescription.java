package fr.firmy.lab.eternity2server.model.dto;

import java.util.Date;

public class SolutionDescription {

    private BoardDescription solution;
    private Date dateSolved;

    public SolutionDescription() {
    }

    public SolutionDescription(BoardDescription solution, Date dateSolved) {
        this.solution = solution;
        this.dateSolved = dateSolved;
    }

    public Date getDateSolved() {
        return dateSolved;
    }

    public void setDateSolved(Date dateSolved) {
        this.dateSolved = dateSolved;
    }

    public BoardDescription getSolution() {
        return solution;
    }

    public void setSolution(BoardDescription solution) {
        this.solution = solution;
    }

}
