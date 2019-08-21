package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.controller.exception.MalformedMaterializedPathException;
import fr.firmy.lab.eternity2server.model.Job;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Node;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NodeAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger( NodeAdapter.class.getName() );

    private final MaterializedPathAdapter materializedPathAdapter;
    private final JobAdapter jobAdapter;

    @Autowired
    public NodeAdapter(MaterializedPathAdapter materializedPathAdapter, JobAdapter jobAdapter) {
        this.materializedPathAdapter = materializedPathAdapter;
        this.jobAdapter = jobAdapter;
    }

    // asNode
    public Node fromJob(Job job) {

        Node node = null;
        BoardDescription boardDescription = jobAdapter.toDescription(job).getJob();
        try {
            MaterializedPath materializedPath = materializedPathAdapter.fromBoardDescription(boardDescription);
            node = new Node(materializedPath, job.getAction());
        } catch (MalformedMaterializedPathException e) {
            LOGGER.error("Should not happen", e);
        }
        return node;
    }

}