package fr.firmy.lab.eternity2server.controller;

import fr.firmy.lab.eternity2server.controller.services.JobsService;
import fr.firmy.lab.eternity2server.controller.dal.SolutionsRepository;
import fr.firmy.lab.eternity2server.controller.services.SanityService;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.Job;
import fr.firmy.lab.eternity2server.model.Solution;
import fr.firmy.lab.eternity2server.model.adapter.JobAdapter;
import fr.firmy.lab.eternity2server.model.adapter.SolutionAdapter;
import fr.firmy.lab.eternity2server.model.adapter.SolverInfoAdapter;
import fr.firmy.lab.eternity2server.model.dto.*;
import fr.firmy.lab.eternity2server.controller.exception.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eternity2-server/v1")
public class HttpController {

    private final JobsService jobsService;
    private final SanityService sanityService;
    private final SolutionsRepository solutionsRepository;

    // adapters
    private final SolutionAdapter solutionAdapter;
    private final JobAdapter jobAdapter;
    private final SolverInfoAdapter solverInfoAdapter;

    @Autowired
    public HttpController(JobsService jobsService, SanityService sanityService, SolutionsRepository solutionsRepository, SolutionAdapter solutionAdapter, JobAdapter jobAdapter, SolverInfoAdapter solverInfoAdapter) {
        this.jobsService = jobsService;
        this.solutionsRepository = solutionsRepository;
        this.solutionAdapter = solutionAdapter;
        this.jobAdapter = jobAdapter;
        this.solverInfoAdapter = solverInfoAdapter;
        this.sanityService = sanityService;
    }

    @GetMapping(value = "/jobs")
    public List<JobDescription> getJobs(@RequestParam(value="size") Integer jobSize,
                                        @RequestParam(value="limit", required = false) Integer jobLimit,
                                        @RequestParam(value="offset", required = false) Integer jobOffset) throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        return jobsService.getJobsToDo(jobSize, jobLimit, jobOffset).stream()
                .map( jobAdapter::toDescription )
                .collect(Collectors.toList());
    }

    @PostMapping(value = "/result")
    public void putResult(@RequestBody ResultDescription result, HttpServletRequest request) throws ResultSubmissionFailedException {

        if( result.getSolutions() != null ) {

            List<ErrorDescription> errors = new ArrayList<>();
            List<Solution> solutions = new ArrayList<>();

            for(SolutionDescription description : result.getSolutions()) {
                try {
                    solutions.add( solutionAdapter.fromDescription( description ) );
                } catch (MalformedSolutionDescriptionException e) {
                    errors.add( new ErrorDescription(HttpStatus.BAD_REQUEST, "/result", "Solution description "+description.toString()+" is malformed") );
                }
            }

            solutionsRepository.addSolutions(solutions);

            if( ! errors.isEmpty() ) {
                throw new ResultSubmissionFailedException( errors );
            }

        } else {
            throw new ResultSubmissionFailedException(Collections.singletonList(new ErrorDescription(HttpStatus.BAD_REQUEST, "/result", "Solutions are missing in the request")));
        }

        if( result.getJob() != null ) {
            try {
                jobsService.declareDone(
                        new Job( jobAdapter.fromDescription(result.getJob()), Action.DONE ),
                        solverInfoAdapter.fromDescription(result.getSolverDescription(), request.getRemoteAddr())
                );
            } catch (JobUpdateFailedException e) {
                throw new ResultSubmissionFailedException(Collections.singletonList(new ErrorDescription(HttpStatus.BAD_REQUEST, result.getJob().getJob().getRepresentation(), "Impossible to update job")), e);
            } catch( JobPruneFailedException e) {
                throw new ResultSubmissionFailedException(Collections.singletonList(new ErrorDescription(HttpStatus.INTERNAL_SERVER_ERROR, result.getJob().getJob().getRepresentation(), "Impossible to prune branches after job update")), e);
            } catch ( MalformedJobDescriptionException e) {
                throw new ResultSubmissionFailedException(Collections.singletonList(new ErrorDescription(HttpStatus.BAD_REQUEST, result.getJob().getJob().getRepresentation(), "Impossible to update job: Job is malformed in the request")), e);
            } catch ( MalformedSolverDescriptionException e) {
                throw new ResultSubmissionFailedException(Collections.singletonList(new ErrorDescription(HttpStatus.BAD_REQUEST, result.getJob().getJob().getRepresentation(), "Impossible to update job: Solver description is malformed in the request")), e);
            }
        } else {
            throw new ResultSubmissionFailedException(Collections.singletonList(new ErrorDescription(HttpStatus.BAD_REQUEST, "/result", "Job is missing in the request")));
        }
    }

    @GetMapping(value = "/solutions")
    public List<SolutionDescription> getSolutions(@RequestParam(value="limit", required = false) Integer resultsLimit,
                                                  @RequestParam(value="offset", required = false) Integer resultsOffset) {

        List<Solution> solutions = solutionsRepository.getSolutions(resultsLimit, resultsOffset);

        return solutions.stream().map(solutionAdapter::toDescription).collect(Collectors.toList());
    }

    @PutMapping(value = "/status")
    public void putStatus(@RequestBody StatusDescription status, HttpServletRequest request) throws JobUpdateFailedException {

        Optional<ErrorDescription> error = Optional.empty();
        if( status.getStatus().equalsIgnoreCase(Action.PENDING.name() ) ) {

            try {
                jobsService.declarePending(
                        new Job(jobAdapter.fromDescription(status.getJob()), Action.PENDING),
                        solverInfoAdapter.fromDescription(status.getSolverDescription(), request.getRemoteAddr())
                );
            } catch(MalformedJobDescriptionException e) {
                error = Optional.of(new ErrorDescription(HttpStatus.BAD_REQUEST, status.getJob().getJob().getRepresentation(), "The given job is malformed in the request"));
            } catch(MalformedSolverDescriptionException e) {
                error = Optional.of(new ErrorDescription(HttpStatus.BAD_REQUEST, status.getJob().getJob().getRepresentation(), "The given solver description is malformed in the request"));
            }

        } else {

            if( status.getStatus().equalsIgnoreCase(Action.GO.name() ) ) {

                try {
                    jobsService.giveUpPending(
                            new Job(jobAdapter.fromDescription(status.getJob()), Action.PENDING),
                            solverInfoAdapter.fromDescription(status.getSolverDescription(), request.getRemoteAddr())
                    );
                } catch(MalformedJobDescriptionException e) {
                    error = Optional.of(new ErrorDescription(HttpStatus.BAD_REQUEST, status.getJob().getJob().getRepresentation(), "The given job is malformed in the request"));
                } catch(MalformedSolverDescriptionException e) {
                    error = Optional.of(new ErrorDescription(HttpStatus.BAD_REQUEST, status.getJob().getJob().getRepresentation(), "The given solver description is malformed in the request"));
                }

            } else {
                if (status.getStatus().equalsIgnoreCase(Action.DONE.name())) {
                    error = Optional.of(new ErrorDescription(HttpStatus.FORBIDDEN, status.getJob().getJob().getRepresentation(), "Please use the /result endpoint to declare finished jobs"));
                }
                else {
                    error = Optional.of(new ErrorDescription(HttpStatus.BAD_REQUEST, status.getJob().getJob().getRepresentation(), "Unknown status. Accepted status: PENDING."));
                }
            }
        }

        if( error.isPresent() ) {
            throw new JobUpdateFailedException(error.get());
        }
    }

    @GetMapping(value="/sanity-check")
    public void sanityCheck() throws TreeSanityCheckFailedException {
        sanityService.check();
    }

}
