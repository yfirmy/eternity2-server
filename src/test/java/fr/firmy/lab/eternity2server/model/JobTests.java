package fr.firmy.lab.eternity2server.model;

import fr.firmy.lab.eternity2server.model.adapter.JobAdapter;
import fr.firmy.lab.eternity2server.model.adapter.NodeAdapter;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import fr.firmy.lab.eternity2server.controller.exception.*;
import fr.firmy.lab.eternity2server.utils.TestDataHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static fr.firmy.lab.eternity2server.model.Action.GO;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobTests {

    private int boardSize = 9;

    @Autowired
    private JobAdapter jobAdapter;

    @Autowired
    private NodeAdapter nodeAdapter;

    @Autowired
    private TestDataHelper testDataHelper;

    @Test
    @Deprecated
    public void test_job_from_description_1() throws MalformedBoardDescriptionException {
        JobDescription description = new JobDescription( new BoardDescription("#.:.:.:.:.:.:.:.:.:;", boardSize) );
        Job job = jobAdapter.fromDescription(description);
        assertThat(jobAdapter.toDescription(job).getJob().getRepresentation()).as("Full Job description").isEqualTo(description.getJob().getRepresentation());
    }

    @Test
    @Deprecated
    public void test_job_from_description_2() throws MalformedBoardDescriptionException {
        JobDescription description = new JobDescription( new BoardDescription( "#212W:203S:.:.:.:.:.:.:.:;", boardSize) );
        Job job = jobAdapter.fromDescription(description);
        assertThat(jobAdapter.toDescription(job).getJob().getRepresentation()).as("Full Job description").isEqualTo(description.getJob().getRepresentation());
    }

    @Test
    public void test_job_from_materializedPath_1() throws MalformedMaterializedPathException {
        Node node = new Node( new MaterializedPath(""), GO);
        Job job = testDataHelper.buildJob("#.:.:.:.:.:.:.:.:.:;");
        assertThat(nodeAdapter.fromJob(job)).as("Job materialized path").isEqualTo(node);
    }

    @Test
    public void test_job_from_materializedPath_2() throws MalformedMaterializedPathException {
        Node node = new Node( new MaterializedPath("212W.203S"), GO);
        Job job = testDataHelper.buildJob("#212W:.:.:.:.:.:203S:.:.:;");
        assertThat(nodeAdapter.fromJob(job)).as("Job materialized path").isEqualTo(node);
    }

    @Test
    public void test_job_is_done_1() throws MalformedMaterializedPathException {
        Job job = testDataHelper.buildJob("#212W:.:.:.:.:.:203S:.:.:;");
        assertThat(job.isDone()).as("Job is done").isFalse();
    }

    @Test
    public void test_job_is_done_2() throws MalformedMaterializedPathException {
        Job job = testDataHelper.buildJob("#212W:.:.:.:.:.:203S:.:.:;");
        assertThat(job.isDone()).as("Job is done").isFalse();
    }

}
