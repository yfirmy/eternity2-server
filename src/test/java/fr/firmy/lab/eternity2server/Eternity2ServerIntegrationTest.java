package fr.firmy.lab.eternity2server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import fr.firmy.lab.eternity2server.utils.TestDataHelper;
import fr.firmy.lab.eternity2server.utils.TestDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.ExpectedCount.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Eternity2ServerApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application.properties")
public class Eternity2ServerIntegrationTest {

    private static final String API_ETERNITY2 = "/api/eternity2-server/v1";

    private static final String API_ETERNITY2_GET_JOBS = API_ETERNITY2 + "/jobs";

    private static final String API_ETERNITY2_PUT_RESULT = API_ETERNITY2 + "/result";

    private static final String API_ETERNITY2_GET_SOLUTIONS = API_ETERNITY2 + "/solutions";

    private static final String API_ETERNITY2_PUT_STATUS = API_ETERNITY2 + "/status";

    @Autowired
    private MockMvc mvc;

    @Autowired
    TestDataLoader testDataLoader;

    @Autowired
    TestDataHelper testDataHelper;

    @Autowired
    RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() throws JsonProcessingException, URISyntaxException {
        testDataLoader.loadEmptyTree();
    }

    @After
    public void cleanUp() {
        testDataLoader.deleteData();
        this.cleanUpMock();
    }

    private void setUpMock(int timesLevel0, int timesLevel1_0, int timesLevel1_1, int timesLevel1_2) throws JsonProcessingException, URISyntaxException, MalformedBoardDescriptionException {

        String url = "http://localhost:8070/api/eternity2-solver/v1/sub-jobs/{job}";

        mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper mapper = new ObjectMapper();

        this.setUpMockResponse(url, timesLevel0, "$.:.:.:.:.:.:.:.:.:;", Arrays.asList("$200W:.:.:.:.:.:.:.:.:;", "$212W:.:.:.:.:.:.:.:.:;",  "$215N:.:.:.:.:.:.:.:.:;", "$213S:.:.:.:.:.:.:.:.:;" ), mapper);

        this.setUpMockResponse(url, timesLevel1_0, "$200W:.:.:.:.:.:.:.:.:;", Collections.emptyList(), mapper);

        this.setUpMockResponse(url, timesLevel1_1, "$212W:.:.:.:.:.:.:.:.:;", Arrays.asList("$212W:.:.:.:.:.:300N:.:.:;", "$212W:.:.:.:.:.:400N:.:.:;"), mapper);

        this.setUpMockResponse(url, timesLevel1_2, "$213S:.:.:.:.:.:.:.:.:;", Arrays.asList("$213S:.:.:.:.:.:201N:.:.:;", "$213S:.:.:.:.:.:202W:.:.:;"), mapper);
    }



    private void setUpMockResponse(String url, int times, String request, List<String> responses, ObjectMapper mapper) throws URISyntaxException, JsonProcessingException, MalformedBoardDescriptionException {

        JobDescription queryJob1 = testDataHelper.buildJobDescription( request );

        List<JobDescription> responseJobs1 = responses.stream().map(testDataHelper::buildJobDescription).collect(Collectors.toList());

        mockServer.expect(times > 0 ? times(times) : never(),
                requestTo(new URI(url.replace("{job}", queryJob1.getJob().getRepresentation()))))
                .andExpect(method(HttpMethod.GET))
                .andRespond( withSuccess(mapper.writeValueAsString(responseJobs1), MediaType.APPLICATION_JSON ));
    }

    private void cleanUpMock() {
        mockServer.reset();
    }

    @Test
    public void get_jobs_nominal_root_job() throws Exception {

        this.setUpMock(0, 0, 0, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "9")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].job", is("$.:.:.:.:.:.:.:.:.:;")));

        mockServer.verify();
    }

    @Test
    public void get_jobs_error_oversize() throws Exception {

        this.setUpMock(0, 0, 0, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("The requested size must be between 0 and 9")))
                .andExpect(jsonPath("$.requestURI", is(API_ETERNITY2_GET_JOBS)))
                .andExpect(jsonPath("$.timestamp", not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void get_jobs_error_missing_param() throws Exception {

        this.setUpMock(0, 0, 0, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Required Integer parameter 'size' is not present")))
                .andExpect(jsonPath("$.requestURI", is(API_ETERNITY2_GET_JOBS)))
                .andExpect(jsonPath("$.timestamp", not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void get_jobs_nominal_with_one_call_to_solver() throws Exception {

        this.setUpMock(1, 0, 0, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "8")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].job", is("$200W:.:.:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[1].job", is("$212W:.:.:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[2].job", is("$213S:.:.:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[3].job", is("$215N:.:.:.:.:.:.:.:.:;")));

        // root must have been removed in that case
        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "9")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        mockServer.verify();
    }

    @Test
    public void get_jobs_nominal_one_call_triggers_two_calls_to_solver() throws Exception {

        this.setUpMock(1, 1,1, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:300N:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[1].job", is("$212W:.:400N:.:.:.:.:.:.:;")));

        mockServer.verify();
    }

    @Test
    public void get_jobs_nominal_second_call_doesnt_trigger_solver() throws Exception {

        this.setUpMock(1, 1, 1, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:300N:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[1].job", is("$212W:.:400N:.:.:.:.:.:.:;")));

        mockServer.verify();

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:300N:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[1].job", is("$212W:.:400N:.:.:.:.:.:.:;")));

        mockServer.verify();
    }



    @Test
    public void get_jobs_nominal_with_pagination_1_1() throws Exception {

        this.setUpMock(1, 1,1, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .param("limit", "1")
                .param("offset", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:400N:.:.:.:.:.:.:;")));


        mockServer.verify();
    }

    @Test
    public void get_jobs_nominal_with_pagination_1__() throws Exception {

        this.setUpMock(1, 1,1, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .param("limit", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:300N:.:.:.:.:.:.:;")));

        mockServer.verify();
    }

    @Test
    public void get_jobs_nominal_with_pagination___1() throws Exception {

        this.setUpMock(1, 1,1, 0);

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .param("offset", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:400N:.:.:.:.:.:.:;")));

        mockServer.verify();
    }

    @Test
    public void put_result_nominal_with_solution() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\", \"solutions\": [ { \"solution\": \"$215N:203S:7E:6N:5W:4S:3E:2N:1W:;\", \"dateSolved\":\"\" } ], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    public void put_result_nominal_triggering_auto_pruning() throws Exception {

        this.setUpMock(1, 1,1, 1);

        // GIVEN : requesting jobs of size 8 (triggers developing branches)

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:300N:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[1].job", is("$212W:.:400N:.:.:.:.:.:.:;")));

        // WHEN : submitting empty results for one job

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$212W:.:300N:.:.:.:.:.:.:;\", \"solutions\": [], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        // THEN : this job is no more available

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].job", is("$212W:.:400N:.:.:.:.:.:.:;")));

        // AND WHEN : submitting empty results for the second job

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$212W:.:400N:.:.:.:.:.:.:;\", \"solutions\": [], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        // THEN they shouldn't be available anymore, and we get another branch developed

        mvc.perform(get(API_ETERNITY2_GET_JOBS)
                .param("size", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].job", is("$213S:.:201N:.:.:.:.:.:.:;")))
                .andExpect(jsonPath("$[1].job", is("$213S:.:202W:.:.:.:.:.:.:;")));

        mockServer.verify();
    }

    @Test
    public void put_result_nominal_with_no_solution() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\", \"solutions\": [], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    public void put_result_error_missing_solutions_field() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\", \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void put_result_error_malformed_solution() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\", \"solutions\": [ { \"solution\": \"215N.203S.8S.7E.6N.5W.4S.3E.2N.1W\", \"dateSolved\":\"\" } ], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void put_result_error_job_doesnt_exist() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$215N:.:.:.:.:.:.:.:.:;\", \"solutions\": [ { \"solution\": \"$215N:203S:7E:6N:5W:4S:3E:2N:1W:;\", \"dateSolved\":\"\" } ], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void put_result_error_empty() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void put_result_error_missing_result() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\" }"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void put_result_error_missing_job() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solutions\": [ { \"solution\": \"$215N:203S:7E:6N:5W:4S:3E:2N:1W:;\", \"dateSolved\": \"\" } ]}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(isEmptyString())));

        mockServer.verify();
    }

    @Test
    public void put_result_malformed_job() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \".:.:.:.:.:.:.:.:.:.:;\", \"solutions\": [ { \"solution\": \"$215N:203S:7E:6N:5W:4S:3E:2N:1W:;\", \"dateSolved\": \"\" }], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockServer.verify();
    }

    @Test
    public void put_result_wrong_length_solution() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\", \"solutions\": [ { \"solution\": \"$215N:203S:7E:6N:5W:4S:3E:2N:;\", \"dateSolved\": \"\" }], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockServer.verify();
    }

    @Test
    public void get_solutions_nominal() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_RESULT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\", \"solutions\": [ { \"solution\": \"$1N:5N:3S:8E:9N:6W:2W:7S:4E:;\", \"dateSolved\": \"\"}, {\"solution\": \"$215N:203S:7E:6N:5W:4S:3E:2N:1W:;\", \"dateSolved\": \"\"} ], \"dateJobTransmission\": \"\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        mvc.perform(get(API_ETERNITY2_GET_SOLUTIONS)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].solution", is("$1N:5N:3S:8E:9N:6W:2W:7S:4E:;")))
                .andExpect(jsonPath("$[1].solution", is("$215N:203S:7E:6N:5W:4S:3E:2N:1W:;")))
        ;


        mockServer.verify();
    }

    @Test
    public void put_status_nominal() throws Exception {

        this.setUpMock(0, 0,0, 0);

        mvc.perform(put(API_ETERNITY2_PUT_STATUS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"job\": \"$.:.:.:.:.:.:.:.:.:;\", \"status\": \"PENDING\", \"dateJobTransmission\": \"\", \"dateStatusUpdate\": \"\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        mockServer.verify();
    }

}