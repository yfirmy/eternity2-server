package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.controller.exception.MalformedSolutionDescriptionException;
import fr.firmy.lab.eternity2server.model.Solution;
import fr.firmy.lab.eternity2server.model.dto.SolutionDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SolutionAdapter {

    private final MaterializedPathAdapter materializedPathAdapter;

    @Autowired
    public SolutionAdapter(MaterializedPathAdapter materializedPathAdapter) {
        this.materializedPathAdapter = materializedPathAdapter;
    }

    public SolutionDescription toDescription(Solution solution) {
        return new SolutionDescription(
                materializedPathAdapter.toBoardDescription(solution.getPath()),
                solution.getDateSolved());
    }

    public Solution fromDescription(SolutionDescription solutionDescription) throws MalformedSolutionDescriptionException {
        Solution result;
        try {
            result = new Solution(
                    materializedPathAdapter.fromBoardDescription(solutionDescription.getSolution()),
                    solutionDescription.getDateSolved());
        }
        catch(MalformedBoardDescriptionException e) {
            throw new MalformedSolutionDescriptionException(solutionDescription, e);
        }
        return result;
    }

}