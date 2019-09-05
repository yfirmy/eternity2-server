package fr.firmy.lab.eternity2server.controller.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.firmy.lab.eternity2server.model.Job;
import fr.firmy.lab.eternity2server.model.adapter.JobAdapter;
import fr.firmy.lab.eternity2server.model.adapter.NodeAdapter;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import fr.firmy.lab.eternity2server.controller.exception.*;

import fr.firmy.lab.eternity2server.utils.TestDataHelper;
import fr.firmy.lab.eternity2server.utils.TestDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static fr.firmy.lab.eternity2server.model.Action.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobsServiceTests {

    private static String subJobsRequest = "http://localhost:8070/api/eternity2-solver/v1/sub-jobs/{job}";

    @Autowired
    JobsService jobsService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TestDataLoader testDataLoader;

    @Autowired
    TestDataHelper testDataHelper;

    @Autowired
    JobAdapter jobAdapter;

    @Autowired
    NodeAdapter nodeAdapter;

    @Before
    public void setUp() {
        testDataLoader.loadData();
    }

    @After
    public void cleanUp() {
        testDataLoader.deleteData();
    }

    @Test
    public void test_getDoneJobs() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<6; i++) {
            List<Job> doneJobs = jobsService.getDoneJobs(i);
            assertThat(doneJobs.size()).as("Done getJobs of size "+i).isEqualTo(0);
        }

        List<Job> doneJobs6 = jobsService.getDoneJobs(6);
        assertThat(jobAdapter.toDescription(doneJobs6.get(0)).getJob().getRepresentation()).as("Description of found done job of size 6").isEqualTo("$215N:.:200N:.:.:.:700E:.:.:;");
        assertThat(doneJobs6.size()).as("Done getJobs of size 6").isEqualTo(1);

        List<Job> doneJobs7 = jobsService.getDoneJobs(7);
        assertThat(jobAdapter.toDescription(doneJobs7.get(0)).getJob().getRepresentation()).as("Description of found done job of size 7").isEqualTo("$213S:.:202W:.:.:.:.:.:.:;");
        assertThat(doneJobs7.size()).as("Done getJobs of size 7").isEqualTo(1);

        List<Job> doneJobs8 = jobsService.getDoneJobs(8);
        assertThat(jobAdapter.toDescription(doneJobs8.get(0)).getJob().getRepresentation()).as("Description of found done job of size 8").isEqualTo("$200W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobs8.size()).as("Done getJobs of size 8").isEqualTo(1);
    }

    @Test
    public void test_getPendingJobs() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<=5; i++) {
            List<Job> doneJobs = jobsService.getPendingJobs(i);
            assertThat(doneJobs.size()).as("Pending getJobs of size "+i).isEqualTo(0);
        }

        List<Job> doneJobs6 = jobsService.getPendingJobs(6);
        assertThat(jobAdapter.toDescription(doneJobs6.get(0)).getJob().getRepresentation()).as("Description of found pending job of size 6").isEqualTo("$215N:.:200N:.:.:.:800E:.:.:;");
        assertThat(doneJobs6.size()).as("Pending getJobs of size 6").isEqualTo(1);

        for( int i=7; i<=8; i++) {
            List<Job> doneJobs = jobsService.getPendingJobs(i);
            assertThat(doneJobs.size()).as("Pending getJobs of size "+i).isEqualTo(0);
        }
    }



    private List<String> getJobsToDo_NoDevelop(int size) throws JobSizeException, JobRetrievalFailedException {
        return getJobsToDo_NoDevelop(size, null, null);
    }

    private List<String> getJobsToDo_NoDevelop(int size, Integer limit, Integer offset) throws JobSizeException, JobRetrievalFailedException {
        return jobsService.getJobsToDo_NoDevelop(size, limit, offset)
                .stream().map( jobAdapter::toDescription ).map( JobDescription::getJob).map( BoardDescription::getRepresentation ).collect(Collectors.toList());
    }

    private List<String> getJobsToDo(int size) throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {
        return jobsService.getJobsToDo(size, null, null)
                .stream().map( jobAdapter::toDescription ).map( JobDescription::getJob).map( BoardDescription::getRepresentation ).collect(Collectors.toList());
    }

    private List<String> getDoneJobs(int size) throws JobSizeException, JobRetrievalFailedException {
        return jobsService.getDoneJobs(size)
                .stream().map( jobAdapter::toDescription ).map( JobDescription::getJob).map( BoardDescription::getRepresentation ).collect(Collectors.toList());
    }

    @Test
    public void test_getJobsToDo_NoDevelop_1() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<=6; i++) {
            List<Job> doneJobs = jobsService.getJobsToDo_NoDevelop(i, null, null);
            assertThat(doneJobs.size()).as("Done getJobs of size "+i).isEqualTo(0);
        }

        List<String> doneJobDescriptions7 = getJobsToDo_NoDevelop(7);
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 8").isEqualTo(2);

        List<String> doneJobDescriptions8 = getJobsToDo_NoDevelop(8);
        assertThat(doneJobDescriptions8).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 8").isEqualTo(1);
    }

    @Test
    public void test_getJobsToDo_NoDevelop_limit2() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<=6; i++) {
            List<Job> doneJobs = jobsService.getJobsToDo_NoDevelop(i, null, null);
            assertThat(doneJobs.size()).as("Done getJobs of size "+i).isEqualTo(0);
        }

        List<String> doneJobDescriptions7 = getJobsToDo_NoDevelop(7, 2, null);
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 7").isEqualTo(2);

        List<String> doneJobDescriptions8 = getJobsToDo_NoDevelop(8);
        assertThat(doneJobDescriptions8).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 8").isEqualTo(1);
    }

    @Test
    public void test_getJobsToDo_NoDevelop_limit2_offset0() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<=6; i++) {
            List<Job> doneJobs = jobsService.getJobsToDo_NoDevelop(i, null, null);
            assertThat(doneJobs.size()).as("Done getJobs of size "+i).isEqualTo(0);
        }

        List<String> doneJobDescriptions7 = getJobsToDo_NoDevelop(7, 2, 0);
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 7").isEqualTo(2);

        List<String> doneJobDescriptions8 = getJobsToDo_NoDevelop(8);
        assertThat(doneJobDescriptions8).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 8").isEqualTo(1);
    }

    @Test
    public void test_getJobsToDo_NoDevelop_limit2_offset1() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<=6; i++) {
            List<Job> doneJobs = jobsService.getJobsToDo_NoDevelop(i, null, null);
            assertThat(doneJobs.size()).as("Done getJobs of size "+i).isEqualTo(0);
        }

        List<String> doneJobDescriptions7 = getJobsToDo_NoDevelop(7, 2, 1);
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").doesNotContain("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 7").isEqualTo(1);

        List<String> doneJobDescriptions8 = getJobsToDo_NoDevelop(8);
        assertThat(doneJobDescriptions8).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 8").isEqualTo(1);
    }

    @Test
    public void test_getJobsToDo_NoDevelop_limit1_offset1() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<=6; i++) {
            List<Job> doneJobs = jobsService.getJobsToDo_NoDevelop(i, null, null);
            assertThat(doneJobs.size()).as("Done getJobs of size "+i).isEqualTo(0);
        }

        List<String> doneJobDescriptions7 = getJobsToDo_NoDevelop(7, 1, 1);
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").doesNotContain("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 7").isEqualTo(1);

        List<String> doneJobDescriptions8 = getJobsToDo_NoDevelop(8);
        assertThat(doneJobDescriptions8).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 8").isEqualTo(1);
    }

    @Test
    public void test_getJobsToDo_NoDevelop_limit2_offset2() throws JobSizeException, JobRetrievalFailedException {

        for( int i=1; i<=6; i++) {
            List<Job> doneJobs = jobsService.getJobsToDo_NoDevelop(i, null, null);
            assertThat(doneJobs.size()).as("Done getJobs of size "+i).isEqualTo(0);
        }

        List<String> doneJobDescriptions7 = getJobsToDo_NoDevelop(7, 2, 2);
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 7").isEqualTo(0);

        List<String> doneJobDescriptions8 = getJobsToDo_NoDevelop(8);
        assertThat(doneJobDescriptions8).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 8").isEqualTo(1);
    }

    @Test
    public void test_getJobsToDo_developing_simple() throws JobDevelopmentFailedException, URISyntaxException, JsonProcessingException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:.:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:.:;", "$213S:.:201N:.:.:.:500W:.:.:;"));

        // Size 8 : no branch development necessary
        List<String> doneJobDescriptions8 = getJobsToDo(8);
        assertThat(doneJobDescriptions8).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 8").isEqualTo(1);

        // Size 7 : no branch development necessary
        List<String> doneJobDescriptions7 = getJobsToDo(7);
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 7").isEqualTo(2);

        // Size 6 : branch development is triggered
        List<String> doneJobDescriptions6 = getJobsToDo(6);
        assertThat(doneJobDescriptions6).as("Description of found getJobs to do of size 6").contains("$213S:.:201N:.:.:.:400E:.:.:;");
        assertThat(doneJobDescriptions6).as("Description of found getJobs to do of size 6").contains("$213S:.:201N:.:.:.:500W:.:.:;");
        assertThat(doneJobDescriptions6.size()).as("Jobs to do of size 6").isEqualTo(2);

        mockServer.verify();
    }

    @Test
    public void test_getJobsToDo_developing_cascade() throws JobDevelopmentFailedException, URISyntaxException, JsonProcessingException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:.:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:.:;", "$213S:.:201N:.:.:.:500W:.:.:;"));

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:400E:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:600W:;", "$213S:.:201N:.:.:.:400E:.:700W:;", "$213S:.:201N:.:.:.:400E:.:800W:;"));

        // Size 6 : branch development is triggered
        List<String> doneJobDescriptions5 = getJobsToDo(5);
        assertThat(doneJobDescriptions5).as("Description of found getJobs to do of size 5").contains("$213S:.:201N:.:.:.:400E:.:600W:;");
        assertThat(doneJobDescriptions5).as("Description of found getJobs to do of size 5").contains("$213S:.:201N:.:.:.:400E:.:700W:;");
        assertThat(doneJobDescriptions5).as("Description of found getJobs to do of size 5").contains("$213S:.:201N:.:.:.:400E:.:800W:;");
        assertThat(doneJobDescriptions5.size()).as("Jobs to do of size 5").isEqualTo(3);

        mockServer.verify();
    }

    @Test
    public void test_getJobsToDo_dead_end() throws JobDevelopmentFailedException, URISyntaxException, JsonProcessingException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:.:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:.:;", "$213S:.:201N:.:.:.:500W:.:.:;"));

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:400E:.:.:;",
                Collections.emptyList());

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:500W:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:500W:.:600S:;", "$213S:.:201N:.:.:.:500W:.:700S:;") );

        // Size 6 : branch development is triggered, first meeting a dead end, then finding
        List<String> jobsTodoDescriptions5 = getJobsToDo(5);
        assertThat(jobsTodoDescriptions5).as("Developed Jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:600S:;");
        assertThat(jobsTodoDescriptions5).as("Developed Jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:700S:;");
        assertThat(jobsTodoDescriptions5.size()).as("Developed Jobs to do of size 5").isEqualTo(2);

        // Size 6 again : won't try to develop branch
        List<String> jobsTodoDescriptions5_again = getJobsToDo(5);
        assertThat(jobsTodoDescriptions5_again).as("Retrieved jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:600S:;");
        assertThat(jobsTodoDescriptions5_again).as("Retrieved jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:700S:;");
        assertThat(jobsTodoDescriptions5_again.size()).as("Retrieved Jobs to do of size 5").isEqualTo(2);

        // Jobs done of size 6 contains $213S:201N:400E:.:.:.:.:.:.:;
        List<String> doneJobs6 = getDoneJobs(6);
        assertThat(doneJobs6).as("Description of done Jobs of size 6").contains("$213S:.:201N:.:.:.:400E:.:.:;");
        assertThat(doneJobs6).as("Description of done Jobs of size 6").contains("$215N:.:200N:.:.:.:700E:.:.:;");
        assertThat(doneJobs6.size()).as("Done Jobs of size 6").isEqualTo(2);

        mockServer.verify();
    }

    @Test
    public void test_pruneJobs_nominal1() throws JobPruneFailedException, JobSizeException, JobRetrievalFailedException {

        testDataLoader.insertPath("214W.203S", DONE);
        testDataLoader.insertPath("214W.204S", DONE);

        List<String> prePathsLevel2 = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(prePathsLevel2).as("Pre-condition: Jobs to do of size 7").contains("214W.203S.DONE");
        assertThat(prePathsLevel2).as("Pre-condition: Jobs to do of size 7").contains("214W.204S.DONE");
        assertThat(prePathsLevel2).as("Pre-condition: Jobs to do of size 7").contains("213S.202W.DONE");
        assertThat(prePathsLevel2.size()).as("Pre-condition: Jobs to do count of size 7").isEqualTo(3);

        jobsService.pruneJobs( testDataHelper.buildJob("$214W:.:.:.:.:.:203S:.:.:;", DONE) );

        List<String> pathsLevel1 = jobsService.getDoneJobs(8).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel1).as("Jobs to do of size 8").contains("214W.DONE");
        assertThat(pathsLevel1).as("Jobs to do of size 8").contains("200W.DONE");
        assertThat(pathsLevel1.size()).as("Jobs to do count of size 8").isEqualTo(2);

        List<String> postPathsLevel2 = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(postPathsLevel2).as("Jobs to do of size 7").contains("213S.202W.DONE");
        assertThat(postPathsLevel2.size()).as("Jobs to do count of size 7").isEqualTo(1);
    }

    @Test
    public void test_pruneJobs_nominal2() throws JobPruneFailedException, JobSizeException, JobRetrievalFailedException, JobUpdateFailedException {

        jobsService.declareDone( testDataHelper.buildJob("$213S:.:201N:.:.:.:.:.:.:;", DONE) );

        List<String> prePathsLevel2 = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(prePathsLevel2).as("Pre-condition: Jobs to do of size 7").doesNotContain("213S.201N.DONE");
        assertThat(prePathsLevel2.size()).as("Pre-condition: Jobs to do count of size 7").isEqualTo(0);

        List<String> pathsLevel1 = jobsService.getDoneJobs(8).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel1).as("Jobs to do of size 8").contains("213S.DONE");
        assertThat(pathsLevel1).as("Jobs to do of size 8").contains("200W.DONE");
        assertThat(pathsLevel1.size()).as("Jobs to do count of size 8").isEqualTo(2);

        jobsService.declareDone( testDataHelper.buildJob("$215N:.:203S:.:.:.:.:.:.:;", DONE) );
        jobsService.declareDone( testDataHelper.buildJob("$215N:.:200N:.:.:.:800E:.:.:;", DONE) );
        jobsService.declareDone( testDataHelper.buildJob("$212W:.:.:.:.:.:.:.:.:;", DONE) );

        List<String> postPathsLevel1bis = jobsService.getDoneJobs(8).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(postPathsLevel1bis.size()).as("Jobs to do count of size 8").isEqualTo(0);

        List<String> postPathsLevel0 = jobsService.getDoneJobs(9).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(postPathsLevel0).as("Jobs to do of size 9").contains("DONE");
        assertThat(postPathsLevel0.size()).as("Jobs to do count of size 9").isEqualTo(1);
    }

    @Test
    public void test_pruneJobs_impossible1() throws JobPruneFailedException, JobSizeException, JobRetrievalFailedException {

        testDataLoader.insertPath("214W.203S", DONE);
        testDataLoader.insertPath("214W.204S.404N", DONE);

        jobsService.pruneJobs( testDataHelper.buildJob("$214W:.:203S:.:.:.:.:.:.:;") );

        List<String> pathsLevel1 = jobsService.getDoneJobs(8).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel1).as("Done getJobs to do of size 8").contains("200W.DONE");
        assertThat(pathsLevel1.size()).as("Done getJobs count of size 8").isEqualTo(1);

        List<String> pathsLevel2 = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2).as("Done getJobs of size 7").contains("213S.202W.DONE");
        assertThat(pathsLevel2).as("done getJobs of size 7").contains("214W.203S.DONE");
        assertThat(pathsLevel2.size()).as("Done getJobs to do of size 7").isEqualTo(2);
    }

    @Test
    public void test_pruneJobs_impossible2() throws JobPruneFailedException, JobSizeException, JobRetrievalFailedException {

        testDataLoader.insertPath("214W.203S", DONE);
        testDataLoader.insertPath("214W.204S", GO);

        jobsService.pruneJobs( testDataHelper.buildJob("$214W:.:203S:.:.:.:.:.:.:;") );

        List<String> pathsLevel1 = jobsService.getDoneJobs(8).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel1).as("Jobs to do of size 8").contains("200W.DONE");
        assertThat(pathsLevel1.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> pathsLevel2 = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2).as("Jobs to do of size 7").contains("213S.202W.DONE");
        assertThat(pathsLevel2).as("Jobs to do of size 7").contains("214W.203S.DONE");
        assertThat(pathsLevel2.size()).as("Jobs to do of size 7").isEqualTo(2);
    }

    @Test
    public void test_developBranchOfJobsFromJob_nominal() throws JsonProcessingException, URISyntaxException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper mapper = new ObjectMapper();

        String queryJobStr = "$213S:.:201N:.:.:.:.:.:.:;";

        List<JobDescription> responseJobs = Arrays.asList(
                testDataHelper.buildJobDescription("$213S:.:201N:.:.:.:400E:.:.:;"),
                testDataHelper.buildJobDescription("$213S:.:201N:.:.:.:500W:.:.:;"));

        mockServer.expect(once(), requestTo(new URI(subJobsRequest.replace("{job}", queryJobStr))))
            .andExpect(method(HttpMethod.GET))
            .andRespond( withSuccess(mapper.writeValueAsString(responseJobs), MediaType.APPLICATION_JSON ));

        List<String> pathsLevel3_before = jobsService.getJobsToDo_NoDevelop(6, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel3_before.size()).as("Jobs to do of size 6 before expanding").isEqualTo(0);

        List<String> pathsLevel2_before = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("213S.201N.GO");
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("215N.203S.GO");
        assertThat(pathsLevel2_before.size()).as("Jobs to do of size 7 before expanding").isEqualTo(2);

        jobsService.developBranchOfJobsFromJob( testDataHelper.buildJob( queryJobStr ));

        List<String> pathsLevel3_after = jobsService.getJobsToDo_NoDevelop(6, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel3_after).as("Jobs to do of size 6 after expanding").contains("213S.201N.400E.GO");
        assertThat(pathsLevel3_after).as("Jobs to do of size 6 after expanding").contains("213S.201N.500W.GO");
        assertThat(pathsLevel3_after.size()).as("Jobs to do of size 7 after expanding").isEqualTo(2);

        List<String> pathsLevel2_after = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_after).as("Jobs to do of size 7 after expanding").contains("215N.203S.GO");
        assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after expanding").isEqualTo(1);

        mockServer.verify();
    }

    @Test
    public void test_developBranchOfJobsFromJob_empty() throws JsonProcessingException, URISyntaxException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        String queryJob = "$213S:.:201N:.:.:.:.:.:.:;";

        testDataHelper.mockHttpQueryResponse(mockServer, queryJob, Collections.emptyList());

        List<String> pathsLevel3_before = jobsService.getJobsToDo_NoDevelop(6, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel3_before.size()).as("Jobs to do of size 6 before expanding").isEqualTo(0);

        List<String> pathsLevel2_before = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("213S.201N.GO");
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("215N.203S.GO");
        assertThat(pathsLevel2_before.size()).as("Jobs to do of size 7 before expanding").isEqualTo(2);

        List<String> pathsDoneLevel2_before = jobsService.getDoneJobs(7)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsDoneLevel2_before).as("Jobs done of size 7 before expanding").contains("213S.202W.DONE");
        assertThat(pathsDoneLevel2_before.size()).as("Jobs done of size 7 before expanding").isEqualTo(1);

        List<String> pathsDoneLevel1_before = jobsService.getDoneJobs(8)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsDoneLevel1_before).as("Jobs to do of size 8 before expanding").contains("200W.DONE");
        assertThat(pathsDoneLevel1_before.size()).as("Jobs done of size 8 before expanding").isEqualTo(1);

        jobsService.developBranchOfJobsFromJob( testDataHelper.buildJob( queryJob ));

        List<String> pathsLevel3_after = jobsService.getJobsToDo_NoDevelop(6, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel3_after.size()).as("Jobs to do of size 6 after expanding").isEqualTo(0);

        List<String> pathsLevel2_after = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_after).as("Jobs to do of size 7 after expanding").contains("215N.203S.GO");
        assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after expanding").isEqualTo(1);

        List<String> pathsDoneLevel2_after = jobsService.getDoneJobs(7)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsDoneLevel2_after.size()).as("Jobs done of size 7 after expanding").isEqualTo(0);

        List<String> pathsDoneLevel1_after = jobsService.getDoneJobs(8)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsDoneLevel1_after).as("Jobs to do of size 8 after expanding").contains("213S.DONE");
        assertThat(pathsDoneLevel1_after).as("Jobs to do of size 8 after expanding").contains("200W.DONE");
        assertThat(pathsDoneLevel1_after.size()).as("Jobs done of size 8 after expanding").isEqualTo(2);

        mockServer.verify();
    }

    @Test(expected = JobDevelopmentFailedException.class)
    public void test_developBranchOfJobsFromJob_error_solver_results() throws JsonProcessingException, URISyntaxException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper mapper = new ObjectMapper();

        String queryJobStr = "$213S:.:201N:.:.:.:.:.:.:;";

        List<JobDescription> responseJobs = Arrays.asList(
                testDataHelper.buildJobDescription("$.:.:.:.:.:.:.:.:.:;"),
                testDataHelper.buildJobDescription("$213S:.:201N:.:.:.:500W:.:.:;"));

        mockServer.expect(once(), requestTo(new URI(subJobsRequest.replace("{job}", queryJobStr))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                        mapper.writeValueAsString(responseJobs).replace("$.:.:.:.:.:.:.:.:.:;", "$.:.:.:.:.:.:;"),
                        MediaType.APPLICATION_JSON )
                );

        List<String> pathsLevel3_before = jobsService.getJobsToDo_NoDevelop(6, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel3_before.size()).as("Jobs to do of size 6 before expanding").isEqualTo(0);

        List<String> pathsLevel2_before = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("213S.201N.GO");
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("215N.203S.GO");
        assertThat(pathsLevel2_before.size()).as("Jobs to do of size 7 before expanding").isEqualTo(2);

        List<String> pathsDoneLevel2_before = jobsService.getDoneJobs(7)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsDoneLevel2_before).as("Jobs done of size 7 before expanding").contains("213S.202W.DONE");
        assertThat(pathsDoneLevel2_before.size()).as("Jobs done of size 7 before expanding").isEqualTo(1);

        try {
            jobsService.developBranchOfJobsFromJob(testDataHelper.buildJob(queryJobStr));
        } catch( Exception e ) {

            List<String> pathsLevel3_after = jobsService.getJobsToDo_NoDevelop(6, null, null)
                    .stream().map(job -> nodeAdapter.fromJob(job).toString()).collect(Collectors.toList());
            assertThat(pathsLevel3_after.size()).as("Jobs to do of size 6 after expanding").isEqualTo(0);

            List<String> pathsLevel2_after = jobsService.getJobsToDo_NoDevelop(7, null, null)
                    .stream().map(job -> nodeAdapter.fromJob(job).toString()).collect(Collectors.toList());
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after expanding").contains("213S.201N.GO");
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after expanding").contains("215N.203S.GO");
            assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after expanding").isEqualTo(2);

            List<String> pathsDoneLevel2_after = jobsService.getDoneJobs(7)
                    .stream().map(job -> nodeAdapter.fromJob(job).toString()).collect(Collectors.toList());
            assertThat(pathsDoneLevel2_after).as("Jobs done of size 7 after expanding").contains("213S.202W.DONE");
            assertThat(pathsDoneLevel2_after.size()).as("Jobs done of size 7 after expanding").isEqualTo(1);

            mockServer.verify();

            throw e;
        }
    }

    @Test(expected = JobDevelopmentFailedException.class)
    public void test_developBranchOfJobsFromJob_error_replace_failed() throws JsonProcessingException, URISyntaxException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper mapper = new ObjectMapper();

        String queryJobStr = "$213S:.:201N:.:.:.:.:.:.:;";

        List<JobDescription> responseJobs = Arrays.asList(
                testDataHelper.buildJobDescription("$213S:.:201N:.:.:.:400E:.:.:;"),
                testDataHelper.buildJobDescription("$215N:.:203S:.:.:.:.:.:.:;"));

        mockServer.expect(once(), requestTo(new URI(subJobsRequest.replace("{job}", queryJobStr))))
                .andExpect(method(HttpMethod.GET))
                .andRespond( withSuccess(mapper.writeValueAsString(responseJobs), MediaType.APPLICATION_JSON ));

        List<String> pathsLevel3_before = jobsService.getJobsToDo_NoDevelop(6, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel3_before.size()).as("Jobs to do of size 6 before expanding").isEqualTo(0);

        List<String> pathsLevel2_before = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("213S.201N.GO");
        assertThat(pathsLevel2_before).as("Jobs to do of size 7 before expanding").contains("215N.203S.GO");
        assertThat(pathsLevel2_before.size()).as("Jobs to do of size 7 before expanding").isEqualTo(2);

        List<String> pathsDoneLevel2_before = jobsService.getDoneJobs(7)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsDoneLevel2_before).as("Jobs done of size 7 before expanding").contains("213S.202W.DONE");
        assertThat(pathsDoneLevel2_before.size()).as("Jobs done of size 7 before expanding").isEqualTo(1);

        try {
            jobsService.developBranchOfJobsFromJob(testDataHelper.buildJob(queryJobStr));
        } catch( Exception e ) {

            List<String> pathsLevel3_after = jobsService.getJobsToDo_NoDevelop(6, null, null)
                    .stream().map(job -> nodeAdapter.fromJob(job).toString()).collect(Collectors.toList());
            assertThat(pathsLevel3_after.size()).as("Jobs to do of size 6 after expanding").isEqualTo(0);

            List<String> pathsLevel2_after = jobsService.getJobsToDo_NoDevelop(7, null, null)
                    .stream().map(job -> nodeAdapter.fromJob(job).toString()).collect(Collectors.toList());
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after expanding").contains("213S.201N.GO");
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after expanding").contains("215N.203S.GO");
            assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after expanding").isEqualTo(2);

            List<String> pathsDoneLevel2_after = jobsService.getDoneJobs(7)
                    .stream().map(job -> nodeAdapter.fromJob(job).toString()).collect(Collectors.toList());
            assertThat(pathsDoneLevel2_after).as("Jobs done of size 7 after expanding").contains("213S.202W.DONE");
            assertThat(pathsDoneLevel2_after.size()).as("Jobs done of size 7 after expanding").isEqualTo(1);

            mockServer.verify();

            throw e;
        }
    }

    @Test
    public void test_declareDone_nominal() throws JobUpdateFailedException, JobPruneFailedException, JobSizeException, JobRetrievalFailedException {

        Job initialJob = testDataHelper.buildJob( "$215N:.:203S:.:.:.:.:.:.:;" );

        List<String> jobsTodo_before = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("213S.201N.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("215N.203S.GO");
        assertThat(jobsTodo_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(2);

        List<String> jobsDone_before = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsDone_before).as("Jobs done of size 7 before submission").contains("213S.202W.DONE");
        assertThat(jobsDone_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(1);

        jobsService.declareDone( initialJob );

        List<String> pathsLevel2_after = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("213S.201N.GO");
        assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(1);

        List<String> jobsDone_after = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsDone_after).as("Jobs done of size 7 after submission").contains("213S.202W.DONE");
        assertThat(jobsDone_after).as("Jobs done of size 7 after submission").contains("215N.203S.DONE");
        assertThat(jobsDone_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(2);
    }

    @Test
    public void test_declareDone_prune() throws JobUpdateFailedException, JobPruneFailedException, JobSizeException, JobRetrievalFailedException {

        Job initialJob = testDataHelper.buildJob( "$213S:.:201N:.:.:.:.:.:.:;" );

        List<String> jobsTodo_before = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("213S.201N.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("215N.203S.GO");
        assertThat(jobsTodo_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(2);

        List<String> jobsDone_before = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsDone_before).as("Jobs done of size 7 before submission").contains("213S.202W.DONE");
        assertThat(jobsDone_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(1);

        List<String> jobsDone9_before = jobsService.getDoneJobs(8).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsDone9_before).as("Jobs done of size 8 before submission").contains("200W.DONE");
        assertThat(jobsDone9_before.size()).as("Jobs to do of size 8 before submission").isEqualTo(1);

        jobsService.declareDone( initialJob );

        List<String> pathsLevel2_after = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("215N.203S.GO");
        assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(1);

        List<String> jobsDone_after = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsDone_after.size()).as("Done jobs of size 7 after submission").isEqualTo(0);

        List<String> jobsDone9_after = jobsService.getDoneJobs(8).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsDone9_after).as("Jobs done of size 8 after submission").contains("200W.DONE");
        assertThat(jobsDone9_after).as("Jobs done of size 8 after submission").contains("213S.DONE");
        assertThat(jobsDone9_after.size()).as("Jobs to do of size 8 after submission").isEqualTo(2);
    }

    @Test(expected = JobUpdateFailedException.class)
    public void test_declareDone_error() throws Exception {

        Job initialJob = testDataHelper.buildJob( "$215N:.:266S:.:.:.:.:.:.:;" );

        List<String> jobsTodo_before = jobsService.getJobsToDo(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("213S.201N.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("215N.203S.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").doesNotContain("215N.266S.GO");
        assertThat(jobsTodo_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(2);

        List<String> jobsDone_before = jobsService.getDoneJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsDone_before).as("Jobs done of size 7 before submission").contains("213S.202W.DONE");
        assertThat(jobsDone_before).as("Jobs done of size 7 before submission").doesNotContain("215N.266S.DONE");
        assertThat(jobsDone_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(1);

        try {
            jobsService.declareDone( initialJob );
        } catch( Exception  e ) {

            List<String> pathsLevel2_after = jobsService.getJobsToDo(7, null, null)
                    .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("213S.201N.GO");
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("215N.203S.GO");
            assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").doesNotContain("215N.266S.GO");
            assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(2);

            List<String> jobsDone_after = jobsService.getDoneJobs(7).stream()
                    .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
            assertThat(jobsDone_after).as("Jobs done of size 7 after submission").contains("213S.202W.DONE");
            assertThat(jobsDone_after).as("Jobs done of size 7 after submission").doesNotContain("215N.266S.DONE");
            assertThat(jobsDone_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(1);

            throw e;
        }
    }

    @Test
    public void test_declarePending_nominal() throws JobUpdateFailedException, JobSizeException, JobRetrievalFailedException {

        Job initialJob = testDataHelper.buildJob( "$215N:.:203S:.:.:.:.:.:.:;" );

        List<String> jobsTodo_before = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("213S.201N.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("215N.203S.GO");
        assertThat(jobsTodo_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(2);

        List<String> jobsPending_before = jobsService.getPendingJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsPending_before.size()).as("Jobs pending of size 7 before submission").isEqualTo(0);

        jobsService.declarePending( initialJob );

        List<String> pathsLevel2_after = jobsService.getJobsToDo_NoDevelop(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("213S.201N.GO");
        assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(1);

        List<String> jobsPending_after = jobsService.getPendingJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsPending_after).as("Jobs pending of size 7 after submission").contains("215N.203S.PENDING");
        assertThat(jobsPending_after.size()).as("Jobs pending of size 7 after submission").isEqualTo(1);
    }

    @Test(expected = JobUpdateFailedException.class)
    public void test_declarePending_error_not_exists() throws Exception {

        Job initialJob = testDataHelper.buildJob( "$215N:.:266S:.:.:.:.:.:.:;" );

        List<String> jobsTodo_before = jobsService.getJobsToDo(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("213S.201N.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("215N.203S.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").doesNotContain("215N.266S.GO");
        assertThat(jobsTodo_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(2);

        List<String> jobsPending_before = jobsService.getPendingJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsPending_before.size()).as("Jobs pending of size 8 before submission").isEqualTo(0);

        try {
            jobsService.declarePending( initialJob );
        } catch( Exception  e ) {

            List<String> pathsLevel2_after = jobsService.getJobsToDo(7, null, null)
                    .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("213S.201N.GO");
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("215N.203S.GO");
            assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").doesNotContain("215N.266S.GO");
            assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(2);

            List<String> jobsPending_after = jobsService.getPendingJobs(7).stream()
                    .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
            assertThat(jobsPending_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(0);

            throw e;
        }
    }

    @Test(expected = JobUpdateFailedException.class)
    public void test_declarePending_error_malformed() throws Exception {

        List<String> jobsTodo_before = jobsService.getJobsToDo(7, null, null)
                .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("213S.201N.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").contains("215N.203S.GO");
        assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").doesNotContain("215N.266S.GO");
        assertThat(jobsTodo_before.size()).as("Jobs to do of size 7 before submission").isEqualTo(2);

        List<String> jobsPending_before = jobsService.getPendingJobs(7).stream()
                .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
        assertThat(jobsPending_before.size()).as("Jobs pending of size 7 before submission").isEqualTo(0);

        try {
            jobsService.declarePending( jobAdapter.fromDescription( new JobDescription( new BoardDescription("$666W:.:.:.:.:.:.:.:.:;")) ) );
        } catch( Exception  e ) {

            List<String> pathsLevel2_after = jobsService.getJobsToDo(7, null, null)
                    .stream().map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("213S.201N.GO");
            assertThat(pathsLevel2_after).as("Jobs to do of size 7 after submission").contains("215N.203S.GO");
            assertThat(jobsTodo_before).as("Jobs to do of size 7 before submission").doesNotContain("215N.266S.GO");
            assertThat(pathsLevel2_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(2);

            List<String> jobsPending_after = jobsService.getPendingJobs(7).stream()
                    .map( job -> nodeAdapter.fromJob(job).toString() ).collect(Collectors.toList());
            assertThat(jobsPending_after.size()).as("Jobs to do of size 7 after submission").isEqualTo(0);

            throw e;
        }
    }

}
