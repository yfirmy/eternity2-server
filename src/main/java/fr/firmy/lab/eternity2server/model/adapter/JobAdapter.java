package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.controller.exception.MalformedJobDescriptionException;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.Job;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobAdapter {

    private final BoardAdapter boardAdapter;

    @Autowired
    public JobAdapter(BoardAdapter boardAdapter) {
        this.boardAdapter = boardAdapter;
    }

    public JobDescription toDescription(Job job) {
        return new JobDescription(boardAdapter.toDescription(job.getBoard()));
    }

    public Job fromDescription(JobDescription jobDescription, Action action) throws MalformedJobDescriptionException {
        Job result;
        try {
            result = new Job(boardAdapter.fromDescription( jobDescription.getJob() ), action);
        } catch (MalformedBoardDescriptionException e) {
            throw new MalformedJobDescriptionException( jobDescription, e);
        }
        return result;
    }

    public Job fromDescription(JobDescription jobDescription) throws MalformedJobDescriptionException {
        return this.fromDescription(jobDescription, Action.GO);
    }

}