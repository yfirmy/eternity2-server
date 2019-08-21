package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.exception.SolverResultException;
import fr.firmy.lab.eternity2server.model.Job;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Node;
import fr.firmy.lab.eternity2server.model.adapter.JobAdapter;
import fr.firmy.lab.eternity2server.model.adapter.NodeAdapter;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@Component
public class SubJobsService {

    private final String subJobsRequest;
    private final RestTemplate restTemplate;
    private final SearchTreeManager searchTreeManager;

    // adapters
    private final JobAdapter jobAdapter;
    private final NodeAdapter nodeAdapter;

    @Autowired
    public SubJobsService(RestTemplate restTemplate, SearchTreeManager searchTreeManager, ServerConfiguration configuration, JobAdapter jobAdapter, NodeAdapter nodeAdapter) {
        this.restTemplate = restTemplate;
        this.searchTreeManager = searchTreeManager;
        this.subJobsRequest = configuration.getSubJobsRequest();
        this.jobAdapter = jobAdapter;
        this.nodeAdapter = nodeAdapter;
    }

    List<Job> getSubJobs(Job job) throws SolverResultException {

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(subJobsRequest).buildAndExpand(jobAdapter.toDescription(job));

        ResponseEntity<List<JobDescription>> response = restTemplate.exchange(
                uriComponents.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<JobDescription>>(){
                });

        return verifyResponse(response, job);
    }

    private List<Job> verifyResponse(ResponseEntity<List<JobDescription>> response, Job job) throws SolverResultException {
        if( response.getBody() != null ) {
            return verifyResults(response.getBody(), job);
        } else {
            List<ErrorDescription> errors = Collections.singletonList( new ErrorDescription(INTERNAL_SERVER_ERROR, job.toString(), "The sub-jobs call returned an empty body") );
            throw new SolverResultException(errors);
        }
    }

    private List<Job> verifyResults(List<JobDescription> results, Job startingPoint) throws SolverResultException {

        List<Job> jobs = results.stream().map(jobAdapter::fromDescription).collect(Collectors.toList());

        verifyCommonParent(jobs, startingPoint);

        return jobs;
    }

    private void verifyCommonParent(List<Job> jobs, Job startingPoint) throws SolverResultException {

        MaterializedPath parent = nodeAdapter.fromJob(startingPoint).getPath();

        Map<Boolean, List<Node>> childrenNodes = jobs.stream()
                .map(nodeAdapter::fromJob)
                .collect(Collectors.groupingBy( node1 -> searchTreeManager.isParentOf(parent, node1.getPath())));

        if( !childrenNodes.isEmpty() && childrenNodes.containsKey(Boolean.FALSE) && !childrenNodes.get(Boolean.FALSE).isEmpty() ) {

            List<ErrorDescription> errors = childrenNodes.get(Boolean.FALSE).stream()
                    .map(node -> new ErrorDescription(INTERNAL_SERVER_ERROR, node.getPath().toString(), "The returned result is not a child of the initial job"))
                    .collect(Collectors.toList());
            throw new SolverResultException(errors);
        }
    }

}
