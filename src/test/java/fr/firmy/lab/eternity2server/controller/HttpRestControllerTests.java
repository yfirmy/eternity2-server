package fr.firmy.lab.eternity2server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.dto.*;
import fr.firmy.lab.eternity2server.controller.exception.*;
import fr.firmy.lab.eternity2server.utils.TestDataHelper;
import fr.firmy.lab.eternity2server.utils.TestDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HttpRestControllerTests {

    @Autowired
    DataSource dataSource;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    HttpRestController httpRestController;

    @Autowired
    TestDataLoader testDataLoader;

    @Autowired
    TestDataHelper testDataHelper;

    @Before
    public void setUp() {
        testDataLoader.loadData();
    }

    @After
    public void cleanUp() {
        testDataLoader.deleteData();
    }

    // Utils

    private List<String> getJobs(int size) throws JobSizeException, JobDevelopmentFailedException, JobRetrievalFailedException {
        return httpRestController.getJobs(size, null, null)
                .stream().map( JobDescription::getJob).map( BoardDescription::getRepresentation ).collect(Collectors.toList());
    }

    private List<String> getSolutions() {
        return httpRestController.getSolutions(null, null)
                .stream().map( SolutionDescription::getSolution ).map( BoardDescription::getRepresentation ).collect(Collectors.toList());
    }

    // GET /getJobs

    @Test( expected = JobSizeException.class )
    public void test_get_jobs_nojob() throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {
        List<JobDescription> jobs = httpRestController.getJobs(10, null, null);
    }

    @Test
    public void test_get_jobs_rootjob() throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {
        List<JobDescription> jobs = httpRestController.getJobs(9, null, null);
        assertThat(jobs.size()).as("Jobs to do of size 9").isEqualTo(0);
    }

    @Test
    public void test_get_jobs_existingjob_all() throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {
        List<String> jobs9 = getJobs(8);
        assertThat(jobs9).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8 = getJobs(7);
        assertThat(jobs8).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8.size()).as("Jobs to do of size 7").isEqualTo(2);
    }

    @Test
    public void test_get_jobs_existingjob_pagination_nominal_1() throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        List<String> jobs8 = httpRestController.getJobs(7, 1, 1)
                .stream().map( JobDescription::getJob).map( BoardDescription::getRepresentation ).collect(Collectors.toList());;
        assertThat(jobs8).as("Description of found job to do of size 7").doesNotContain("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8.size()).as("Jobs to do of size 7").isEqualTo(1);
    }

    @Test
    public void test_get_jobs_existingjob_pagination_nominal_2() throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        List<String> jobs8 = httpRestController.getJobs(7, 1, null)
                .stream().map( JobDescription::getJob).map( BoardDescription::getRepresentation ).collect(Collectors.toList());;
        assertThat(jobs8).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8).as("Description of found job to do of size 7").doesNotContain("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8.size()).as("Jobs to do of size 7").isEqualTo(1);
    }

    @Test
    public void test_get_jobs_existingjob_pagination_nominal_3() throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        List<String> jobs8 = httpRestController.getJobs(7, null, 1)
                .stream().map( JobDescription::getJob).map( BoardDescription::getRepresentation ).collect(Collectors.toList());;
        assertThat(jobs8).as("Description of found job to do of size 7").doesNotContain("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8.size()).as("Jobs to do of size 7").isEqualTo(1);
    }

    @Test
    public void test_get_jobs_developing_simple() throws JobDevelopmentFailedException, URISyntaxException, JsonProcessingException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        testDataHelper.mockHttpQueryResponse(mockServer, "$213S:.:201N:.:.:.:.:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:.:;", "$213S:.:201N:.:.:.:500W:.:.:;"));

        // Size 9 : no branch development necessary
        List<String> doneJobDescriptions9 = getJobs(8);
        assertThat(doneJobDescriptions9).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions9.size()).as("Jobs to do of size 8").isEqualTo(1);

        // Size 8 : no branch development necessary
        List<String> doneJobDescriptions8 = getJobs(7);
        assertThat(doneJobDescriptions8).as("Description of found getJobs to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8.size()).as("Jobs to do of size 7").isEqualTo(2);

        // Size 7 : branch development is triggered
        List<String> doneJobDescriptions7 = getJobs(6);
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 6").contains("$213S:.:201N:.:.:.:400E:.:.:;");
        assertThat(doneJobDescriptions7).as("Description of found getJobs to do of size 6").contains("$213S:.:201N:.:.:.:500W:.:.:;");
        assertThat(doneJobDescriptions7.size()).as("Jobs to do of size 6").isEqualTo(2);

        mockServer.verify();
    }

    @Test
    public void test_get_jobs_developing_cascade() throws JobDevelopmentFailedException, URISyntaxException, JsonProcessingException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:.:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:.:;", "$213S:.:201N:.:.:.:500W:.:.:;"));

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:400E:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:600W:;", "$213S:.:201N:.:.:.:400E:.:700W:;", "$213S:.:201N:.:.:.:400E:.:800W:;"));

        // Size 6 : branch development is triggered
        List<String> doneJobDescriptions6 = getJobs(5);
        assertThat(doneJobDescriptions6).as("Description of found getJobs to do of size 5").contains("$213S:.:201N:.:.:.:400E:.:600W:;");
        assertThat(doneJobDescriptions6).as("Description of found getJobs to do of size 5").contains("$213S:.:201N:.:.:.:400E:.:700W:;");
        assertThat(doneJobDescriptions6).as("Description of found getJobs to do of size 5").contains("$213S:.:201N:.:.:.:400E:.:800W:;");
        assertThat(doneJobDescriptions6.size()).as("Jobs to do of size 5").isEqualTo(3);

        mockServer.verify();
    }

    @Test
    public void test_get_jobs_developing_dead_end() throws JobDevelopmentFailedException, URISyntaxException, JsonProcessingException, JobSizeException, JobRetrievalFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:.:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:400E:.:.:;", "$213S:.:201N:.:.:.:500W:.:.:;"));

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:400E:.:.:;",
                Collections.emptyList());

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$213S:.:201N:.:.:.:500W:.:.:;",
                Arrays.asList("$213S:.:201N:.:.:.:500W:.:600S:;", "$213S:.:201N:.:.:.:500W:.:700S:;"));

        // Size 6 : branch development is triggered, first meeting a dead end, then finding
        List<String> jobsTodoDescriptions6 = getJobs(5);
        assertThat(jobsTodoDescriptions6).as("Developed Jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:600S:;");
        assertThat(jobsTodoDescriptions6).as("Developed Jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:700S:;");
        assertThat(jobsTodoDescriptions6.size()).as("Developed Jobs to do of size 5").isEqualTo(2);

        // Size 6 again : won't try to develop branch
        List<String> jobsTodoDescriptions6_again = getJobs(5);
        assertThat(jobsTodoDescriptions6_again).as("Retrieved jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:600S:;");
        assertThat(jobsTodoDescriptions6_again).as("Retrieved jobs to do of size 5").contains("$213S:.:201N:.:.:.:500W:.:700S:;");
        assertThat(jobsTodoDescriptions6_again.size()).as("Retrieved Jobs to do of size 5").isEqualTo(2);

        mockServer.verify();
    }




    // PUT /result

    @Test
    public void test_put_result_nominal() throws ResultSubmissionFailedException, JobUpdateFailedException {

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription("$215N:203S:.:.:.:.:.:.:.:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result = testDataHelper.buildResultDescription( "$215N:203S:.:.:.:.:.:.:.:;", Collections.singletonList("$215N:203S:7E:6N:5W:4S:3E:2N:1W:;"), new Date(), new Date(), solver);

        // GIVEN : a locked job
        httpRestController.putStatus(status, servletRequest);

        // WHEN : we submit a result for this job
        httpRestController.postResult(result, servletRequest);

        // THEN : this result has been recorded
        List<String> solutions = getSolutions();
        assertThat(solutions.size()).as("Solutions").isEqualTo(2);
        assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
        assertThat(solutions).as("Solutions found").contains("$215N:203S:7E:6N:5W:4S:3E:2N:1W:;");
    }

    @Test
    public void test_put_result_nominal_no_solution() throws ResultSubmissionFailedException, JobUpdateFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription("$215N:203S:.:.:.:.:.:.:.:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result = testDataHelper.buildResultDescription("$215N:203S:.:.:.:.:.:.:.:;", new ArrayList<>(), new Date(), new Date(), solver);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        // GIVEN : a locked job
        httpRestController.putStatus(status, servletRequest);

        // WHEN : we submit a empty result for this job
        httpRestController.postResult(result, servletRequest);

        // THEN : nothing has changed, always the same result is recorded
        List<String> solutions = getSolutions();
        assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
        assertThat(solutions.size()).as("Solutions").isEqualTo(1);
    }

    @Test
    public void test_put_result_nominal_trigger_auto_pruning() throws ResultSubmissionFailedException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException, JobUpdateFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status1 = testDataHelper.buildStatusDescription("$213S:.:201N:.:.:.:.:.:.:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result1 = testDataHelper.buildResultDescription("$213S:.:201N:.:.:.:.:.:.:;", new ArrayList<>(), new Date(), new Date(), solver);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        // GIVEN : a locked job
        httpRestController.putStatus(status1, servletRequest);

        // WHEN : we submit a empty result for this job
        httpRestController.postResult(result1, servletRequest);

        // THEN : no other result is recorded
        List<String> solutions = getSolutions();
        assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
        assertThat(solutions.size()).as("Solutions").isEqualTo(1);

        // AND : this job is not available anymore
        List<String> doneJobDescriptions8_1 = getJobs(7);
        assertThat(doneJobDescriptions8_1).as("Description of found getJobs to do of size 7").doesNotContain("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_1).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_1.size()).as("Jobs to do of size 7").isEqualTo(1);
    }

    @Test
    public void test_put_result_nominal_trigger_auto_pruning_and_more_branches_development() throws ResultSubmissionFailedException, JobDevelopmentFailedException, JobSizeException, URISyntaxException, JsonProcessingException, JobRetrievalFailedException, JobUpdateFailedException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        testDataHelper.mockHttpQueryResponse(mockServer,
                "$212W:.:.:.:.:.:.:.:.:;", Arrays.asList("$212W:.:300N:.:.:.:.:.:.:;", "$212W:.:400N:.:.:.:.:.:.:;"));

        SolverDescription solver = testDataHelper.buildSolverDescription();

        StatusDescription status1 = testDataHelper.buildStatusDescription("$213S:.:201N:.:.:.:.:.:.:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result1 = testDataHelper.buildResultDescription("$213S:.:201N:.:.:.:.:.:.:;", new ArrayList<>(), new Date(), new Date(), solver);

        StatusDescription status2 = testDataHelper.buildStatusDescription("$215N:.:203S:.:.:.:.:.:.:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result2 = testDataHelper.buildResultDescription("$215N:.:203S:.:.:.:.:.:.:;", new ArrayList<>(), new Date(), new Date(), solver);

        httpRestController.putStatus(status1, servletRequest);
        httpRestController.postResult(result1, servletRequest);

        List<String> solutions = getSolutions();
        assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
        assertThat(solutions.size()).as("Solutions").isEqualTo(1);

        List<String> doneJobDescriptions8_1 = getJobs(7);
        assertThat(doneJobDescriptions8_1).as("Description of found getJobs to do of size 7").doesNotContain("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_1).as("Description of found getJobs to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_1.size()).as("Jobs to do of size 7").isEqualTo(1);

        httpRestController.putStatus(status2, servletRequest);
        httpRestController.postResult(result2, servletRequest);

        List<String> doneJobDescriptions8_2 = getJobs(7);
        assertThat(doneJobDescriptions8_2).as("Description of found getJobs to do of size 7").doesNotContain("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_2).as("Description of found getJobs to do of size 7").doesNotContain("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_2).as("Description of found getJobs to do of size 7").contains("$212W:.:300N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_2).as("Description of found getJobs to do of size 7").contains("$212W:.:400N:.:.:.:.:.:.:;");
        assertThat(doneJobDescriptions8_2.size()).as("Jobs to do of size 7").isEqualTo(2);

        mockServer.verify();
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_put_result_error_jobmalformed() throws ResultSubmissionFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        ResultDescription result = testDataHelper.buildResultDescription( "$10N:9W:.:.:.:.:.:.:;", Collections.singletonList("$9W:8S:7E:6N:5W:4S:3E:2N:1W:;"), new Date(), new Date(), solver);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        try {
            httpRestController.postResult(result, servletRequest);
        } catch(Exception e) {

            List<String> solutions = getSolutions();
            assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
            assertThat(solutions).as("Solutions found").contains("$9W:8S:7E:6N:5W:4S:3E:2N:1W:;");
            assertThat(solutions.size()).as("Solutions").isEqualTo(2);

            throw e;
        }
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_put_result_error_job_not_exist() throws ResultSubmissionFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        ResultDescription result = testDataHelper.buildResultDescription( "$99N:.:.:.:.:.:.:.:.:;", Collections.singletonList("$9W:8S:7E:6N:5W:4S:3E:2N:1W:;"), new Date(), new Date(), solver);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        try {
            httpRestController.postResult(result, servletRequest);
        } catch(Exception e) {

            List<String> solutions = getSolutions();
            assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
            assertThat(solutions).as("Solutions found").contains("$9W:8S:7E:6N:5W:4S:3E:2N:1W:;");
            assertThat(solutions.size()).as("Solutions").isEqualTo(2);

            throw e;
        }
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_put_result_error_empty_1() throws ResultSubmissionFailedException {

        ResultDescription result = new ResultDescription();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        try {
            httpRestController.postResult(result, servletRequest);
        } catch( Exception e ) {
            List<String> solutions = getSolutions();
            assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
            assertThat(solutions.size()).as("Solutions").isEqualTo(1);
            throw e;
        }
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_put_result_error_empty_2() throws ResultSubmissionFailedException, JobUpdateFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        //StatusDescription status = testDataHelper.buildStatusDescription("$10N:9W:.:.:.:.:.:.:.:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result = testDataHelper.buildResultDescription("$10N:9W:.:.:.:.:.:.:.:;", null, new Date(), new Date(), solver);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        //httpController.putStatus(status, servletRequest);

        try {
            httpRestController.postResult(result, servletRequest);
        } catch( Exception e ) {
            List<String> solutions = getSolutions();
            assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
            assertThat(solutions.size()).as("Solutions").isEqualTo(1);
            throw e;
        }
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_put_result_error_empty_3() throws ResultSubmissionFailedException, JobUpdateFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        //StatusDescription status = testDataHelper.buildStatusDescription("$9W:8S:7E:6N:5W:4S:3E:2N:1W:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result = testDataHelper.buildResultDescription("", Collections.singletonList("$9W:8S:7E:6N:5W:4S:3E:2N:1W:;"), new Date(), new Date(), solver);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        //httpController.putStatus(status, servletRequest);

        try {
            httpRestController.postResult(result, servletRequest);
        } catch( Exception e ) {
            List<String> solutions = getSolutions();
            assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
            assertThat(solutions).as("Solutions found").contains("$9W:8S:7E:6N:5W:4S:3E:2N:1W:;");
            assertThat(solutions.size()).as("Solutions").isEqualTo(2);
            throw e;
        }
    }


    @Test( expected = ResultSubmissionFailedException.class )
    public void test_put_result_error_malformed_solution() throws ResultSubmissionFailedException, JobUpdateFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        //StatusDescription status = testDataHelper.buildStatusDescription("$215N:203S:.:.:.:.:.:.:.:.:;", Action.PENDING.name(), new Date(), new Date(), solver);
        ResultDescription result = testDataHelper.buildResultDescription( "$215N:203S:.:.:.:.:.:.:.:.:;", Collections.singletonList("$215N:203S:7E:6N:5W:4S:3E:2N:;"), new Date(), new Date(), solver);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        //httpController.putStatus(status, servletRequest);

        try {
            httpRestController.postResult(result, servletRequest);
        } catch( Exception e ) {
            List<String> solutions = getSolutions();
            assertThat(solutions).as("Solutions found").contains("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
            assertThat(solutions).as("Solutions found").doesNotContain("$215N:203S:7E:6N:5W:4S:3E:2N:;");
            assertThat(solutions.size()).as("Solutions").isEqualTo(1);
            throw e;
        }

    }

    // PUT /status

    @Test
    public void test_put_status_nominal_pending() throws JobUpdateFailedException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription( "$215N:.:203S:.:.:.:.:.:.:;", "PENDING", new Date(), new Date(), solver ) ;
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        List<String> jobs9_before = getJobs(8);
        assertThat(jobs9_before).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_before.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8_before = getJobs(7);
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_before.size()).as("Jobs to do of size 7").isEqualTo(2);

        httpRestController.putStatus(status, servletRequest);

        List<String> jobs9_after = getJobs(8);
        assertThat(jobs9_after).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_after.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8_after = getJobs(7);
        assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_after).as("Description of found job to do of size 7").doesNotContain("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_after.size()).as("Jobs to do of size 7").isEqualTo(1);

    }

    @Test( expected = JobUpdateFailedException.class )
    public void test_put_status_nominal_pending_already() throws JobUpdateFailedException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription( "$215N:.:203S:.:.:.:.:.:.:;", "PENDING", new Date(), new Date(), solver ) ;
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        List<String> jobs9_before = getJobs(8);
        assertThat(jobs9_before).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_before.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8_before = getJobs(7);
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_before.size()).as("Jobs to do of size 7").isEqualTo(2);

        httpRestController.putStatus(status, servletRequest);

        List<String> jobs9_after = getJobs(8);
        assertThat(jobs9_after).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_after.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8_after = getJobs(7);
        assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_after).as("Description of found job to do of size 7").doesNotContain("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_after.size()).as("Jobs to do of size 7").isEqualTo(1);

        try {
            httpRestController.putStatus(status, servletRequest);
        } catch( Exception e ) {
            throw e;
        }

    }

    @Test( expected = JobUpdateFailedException.class )
    public void test_put_status_error_job_not_exists() throws JobUpdateFailedException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription( "$215N:.:.:.:.:.:.:.:.:;", "PENDING", new Date(), new Date(), solver ) ;
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        List<String> jobs9_before = getJobs(8);
        assertThat(jobs9_before).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_before.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8_before = getJobs(7);
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_before.size()).as("Jobs to do of size 7").isEqualTo(2);

        try {
            httpRestController.putStatus(status, servletRequest);
        } catch( Exception e ) {

            List<String> jobs9_after = getJobs(8);
            assertThat(jobs9_after).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
            assertThat(jobs9_after.size()).as("Jobs to do of size 8").isEqualTo(1);

            List<String> jobs8_after = getJobs(7);
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
            assertThat(jobs8_after.size()).as("Jobs to do of size 7").isEqualTo(2);

            throw e;
        }

    }

    @Test( expected = JobUpdateFailedException.class )
    public void test_put_status_error_status_not_exists() throws JobUpdateFailedException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription( "$215N:203S:.:.:.:.:.:.:.:;", "WRONG_STATUS", new Date(), new Date(), solver ) ;
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        List<String> jobs9_before = getJobs(8);
        assertThat(jobs9_before).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_before.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8_before = getJobs(7);
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_before.size()).as("Jobs to do of size 7").isEqualTo(2);

        try {
            httpRestController.putStatus(status, servletRequest);
        } catch( Exception e ) {

            List<String> jobs9_after = getJobs(8);
            assertThat(jobs9_after).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
            assertThat(jobs9_after.size()).as("Jobs to do of size 8").isEqualTo(1);

            List<String> jobs8_after = getJobs(7);
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
            assertThat(jobs8_after.size()).as("Jobs to do of size 7").isEqualTo(2);

            throw e;
        }
    }

    @Test( expected = JobUpdateFailedException.class )
    public void test_put_status_error_status_done_forbidden() throws JobUpdateFailedException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription( "$215N:.:.:.:.:.:203S:.:.:;", "DONE", new Date(), new Date(), solver ) ;
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        List<String> jobs9_before = getJobs(8);
        assertThat(jobs9_before).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_before.size()).as("Jobs to do of size 9").isEqualTo(1);

        List<String> jobs8_before = getJobs(7);
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_before.size()).as("Jobs to do of size 7").isEqualTo(2);

        try {
            httpRestController.putStatus(status, servletRequest);
        } catch( Exception e ) {

            List<String> jobs9_after = getJobs(8);
            assertThat(jobs9_after).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
            assertThat(jobs9_after.size()).as("Jobs to do of size 8").isEqualTo(1);

            List<String> jobs8_after = getJobs(7);
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
            assertThat(jobs8_after.size()).as("Jobs to do of size 7").isEqualTo(2);

            throw e;
        }
    }

    @Test( expected = JobUpdateFailedException.class )
    public void test_put_status_error_status_go_forbidden() throws JobUpdateFailedException, JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        SolverDescription solver = testDataHelper.buildSolverDescription();
        StatusDescription status = testDataHelper.buildStatusDescription( "$215N:.:.:.:.:.:203S:.:.:;", "GO", new Date(), new Date(), solver ) ;
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        List<String> jobs9_before = getJobs(8);
        assertThat(jobs9_before).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
        assertThat(jobs9_before.size()).as("Jobs to do of size 8").isEqualTo(1);

        List<String> jobs8_before = getJobs(7);
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
        assertThat(jobs8_before).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
        assertThat(jobs8_before.size()).as("Jobs to do of size 7").isEqualTo(2);

        try {
            httpRestController.putStatus(status, servletRequest);
        } catch( Exception e ) {

            List<String> jobs9_after = getJobs(8);
            assertThat(jobs9_after).as("Description of found job to do of size 8").contains("$212W:.:.:.:.:.:.:.:.:;");
            assertThat(jobs9_after.size()).as("Jobs to do of size 8").isEqualTo(1);

            List<String> jobs8_after = getJobs(7);
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$213S:.:201N:.:.:.:.:.:.:;");
            assertThat(jobs8_after).as("Description of found job to do of size 7").contains("$215N:.:203S:.:.:.:.:.:.:;");
            assertThat(jobs8_after.size()).as("Jobs to do of size 7").isEqualTo(2);

            throw e;
        }
    }
}
