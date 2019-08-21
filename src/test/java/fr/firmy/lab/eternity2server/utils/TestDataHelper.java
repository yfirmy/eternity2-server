package fr.firmy.lab.eternity2server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.controller.exception.MalformedMaterializedPathException;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.Job;
import fr.firmy.lab.eternity2server.model.Solution;
import fr.firmy.lab.eternity2server.model.adapter.JobAdapter;
import fr.firmy.lab.eternity2server.model.adapter.SolutionAdapter;
import fr.firmy.lab.eternity2server.model.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Component
public class TestDataHelper {

    private final int boardSize;
    private SolutionAdapter solutionAdapter;
    private JobAdapter jobAdapter;
    private static String subJobsRequest = "http://localhost:8070/api/eternity2-solver/v1/sub-jobs/{job}";

    @Autowired
    public TestDataHelper(SolutionAdapter solutionAdapter, JobAdapter jobAdapter, ServerConfiguration configuration) {
        this.solutionAdapter = solutionAdapter;
        this.jobAdapter = jobAdapter;
        this.boardSize = configuration.getBoardSize();
    }

    public JobDescription buildJobDescription(String description) {
        BoardDescription boardDescription = null;
        try {
            boardDescription = new BoardDescription(description, boardSize);
        } catch (MalformedBoardDescriptionException e) {
            // should not happen in context of test data initialization
        }
        return new JobDescription(boardDescription);
    }

    public StatusDescription buildStatusDescription(String job, String status, Date dateJobTransmission, Date dateResult) throws MalformedBoardDescriptionException {
        return new StatusDescription( new JobDescription(new BoardDescription(job, boardSize)), status, dateJobTransmission, dateResult );
    }

    public ResultDescription buildResultDescription(String job, List<String> results, Date dateJobTransmission, Date dateResult) throws MalformedBoardDescriptionException {
        List<SolutionDescription> descriptions = null;
        if( results != null ) {
            descriptions = new ArrayList<>();
            for (String result : results) {
                descriptions.add(new SolutionDescription(new BoardDescription(result, boardSize), new Date()));
            }
        }
        return new ResultDescription( new JobDescription(new BoardDescription(job, boardSize)), descriptions, dateJobTransmission );
    }


    public BoardDescription buildBoardDescription(String description) {
        BoardDescription boardDescription = null;
        try {
            boardDescription = new BoardDescription( description, boardSize );
        }
        catch(MalformedBoardDescriptionException e) {
            // should not happen in context of test data initialization
        }
        return boardDescription;
    }

    public Solution buildSolution(String boardDescription) {
        Solution solution = null;
        try {
            solution = solutionAdapter.fromDescription(
                    new SolutionDescription(
                            buildBoardDescription(boardDescription),
                            new Date() )
            );
        } catch (MalformedMaterializedPathException e) {
            // should not happen in context of test data initialization
        }
        return solution;
    }

    public Job buildJob(String boardDescription, Action action) {
        return jobAdapter.fromDescription( new JobDescription(buildBoardDescription(boardDescription)), action );
    }

    public Job buildJob(String boardDescription) {
        return this.buildJob(boardDescription, Action.GO );
    }

    public void mockHttpQueryResponse(MockRestServiceServer mockServer, String query, List<String> responses) throws URISyntaxException, JsonProcessingException {

        List<JobDescription> jobDescriptions = responses.stream().map(this::buildJobDescription).collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        mockServer.expect(once(), requestTo(new URI(subJobsRequest.replace("{job}", query ))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(jobDescriptions), MediaType.APPLICATION_JSON));
    }

}
