package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.dal.SearchTreeManager;
import fr.firmy.lab.eternity2server.controller.exception.MalformedJobDescriptionException;
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

import java.util.ArrayList;
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

        List<Job> result = new ArrayList<>();
        List<ErrorDescription> errors = new ArrayList<>();

        if( response.getBody() != null ) {
            result.addAll( verifyResults(response.getBody(), job, errors ));
        } else {
            errors.add( new ErrorDescription(INTERNAL_SERVER_ERROR, job.toString(), "The sub-jobs call returned an empty body") );
        }
        if( !errors.isEmpty() ) {
            throw new SolverResultException(errors);
        }
        return result;
    }

    private List<Job> verifyResults(List<JobDescription> results, Job startingPoint, List<ErrorDescription> errors) {

        List<Job> jobs = new ArrayList<>();

        for( JobDescription result : results ) {
            try {
                jobs.add( jobAdapter.fromDescription( result ) );
            }
            catch(MalformedJobDescriptionException e) {
                errors.add( new ErrorDescription(INTERNAL_SERVER_ERROR, result.toString(), "Failed to convert a Job Description" ));
            }
        }

        verifyCommonParent(jobs, startingPoint, errors);

        return jobs;
    }

    private void verifyCommonParent(List<Job> jobs, Job startingPoint, List<ErrorDescription> errors) {

        MaterializedPath parent = nodeAdapter.fromJob(startingPoint).getPath();

        Map<Boolean, List<Node>> childrenNodes = jobs.stream()
                .map(nodeAdapter::fromJob)
                .collect(Collectors.groupingBy( node1 -> searchTreeManager.isParentOf(parent, node1.getPath())));

        if( !childrenNodes.isEmpty() && childrenNodes.containsKey(Boolean.FALSE) && !childrenNodes.get(Boolean.FALSE).isEmpty() ) {

            errors.addAll(childrenNodes.get(Boolean.FALSE).stream()
                    .map(node -> new ErrorDescription(INTERNAL_SERVER_ERROR, node.getPath().toString(), "The returned result is not a child of the initial job"))
                    .collect(Collectors.toList()) );
        }
    }

}
