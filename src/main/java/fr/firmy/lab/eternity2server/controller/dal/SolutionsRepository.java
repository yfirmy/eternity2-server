package fr.firmy.lab.eternity2server.controller.dal;

import fr.firmy.lab.eternity2server.controller.exception.ResultSubmissionFailedException;
import fr.firmy.lab.eternity2server.model.Solution;

import java.util.List;

public interface SolutionsRepository {

    List<Solution> getSolutions(Integer limit, Integer offset);
    void addSolutions(List<Solution> solutions) throws ResultSubmissionFailedException;
}
