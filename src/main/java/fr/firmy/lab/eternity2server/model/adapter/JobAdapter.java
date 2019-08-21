package fr.firmy.lab.eternity2server.model.adapter;

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

    public Job fromDescription(JobDescription jobDescription, Action action) {
        return new Job(boardAdapter.fromDescription( jobDescription.getJob() ), action);
    }

    public Job fromDescription(JobDescription jobDescription) {
        return this.fromDescription(jobDescription, Action.GO);
    }

}