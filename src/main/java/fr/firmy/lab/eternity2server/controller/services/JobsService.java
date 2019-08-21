package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.model.*;
import fr.firmy.lab.eternity2server.model.adapter.BoardAdapter;
import fr.firmy.lab.eternity2server.model.adapter.JobAdapter;
import fr.firmy.lab.eternity2server.model.adapter.MaterializedPathAdapter;
import fr.firmy.lab.eternity2server.model.adapter.NodeAdapter;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.controller.exception.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
public class JobsService {

    private static Logger LOGGER = LoggerFactory.getLogger( JobsService.class.getName() );

    private final SearchTreeManager searchTreeManager;
    private final SubJobsService subJobsService;
    private final int boardSize;

    // adapters
    private JobAdapter jobAdapter;
    private NodeAdapter nodeAdapter;
    private MaterializedPathAdapter materializedPathAdapter;
    private BoardAdapter boardAdapter;

    @Autowired
    public JobsService(SearchTreeManager searchTreeManager, SubJobsService subJobsService, ServerConfiguration configuration,
                       JobAdapter jobAdapter, NodeAdapter nodeAdapter, MaterializedPathAdapter materializedPathAdapter, BoardAdapter boardAdapter) {
        this.searchTreeManager = searchTreeManager;
        this.subJobsService = subJobsService;
        this.boardSize = configuration.getBoardSize();
        this.jobAdapter = jobAdapter;
        this.nodeAdapter = nodeAdapter;
        this.materializedPathAdapter = materializedPathAdapter;
        this.boardAdapter = boardAdapter;
    }

    private List<Job> getJobs(Action action, int jobSize, Integer limit, Integer offset) throws JobSizeException, JobRetrievalFailedException {
        List<Job> jobList = new ArrayList<>();
        if( jobSize > 0 && jobSize <= boardSize ) {
            int level = boardSize - jobSize;
            try {
                for(MaterializedPath materializedPath :  searchTreeManager.getPathsAtLevel(action, level, limit, offset)) {
                    jobList.add( new Job( boardAdapter.fromDescription( materializedPathAdapter.toBoardDescription(materializedPath) ), action) );
                }
            } catch (Exception e) {
                throw new JobRetrievalFailedException(jobSize, e);
            }
        } else {
            throw new JobSizeException(jobSize, boardSize);
        }
        return jobList;
    }

    List<Job> getDoneJobs(int size) throws JobSizeException, JobRetrievalFailedException {
        return getJobs(Action.DONE, size, null, null);
    }

    List<Job> getPendingJobs(int size) throws JobSizeException, JobRetrievalFailedException {
        return getJobs(Action.PENDING, size, null, null);
    }

    private List<Job> findJobsTodoGreaterThan(int size, Integer limit, Integer offset) throws JobSizeException, JobRetrievalFailedException {
        List<Job> foundJobs;
        do {
            foundJobs = getJobs(Action.GO, size, limit, offset);
            size++;
        }
        while( foundJobs.isEmpty() && size <= boardSize );

        return foundJobs;
    }

    /*
     * Recursive method to develop a branch from a starting job, to a given job size
     */
    private List<Job> developBranchOfJobsFromJobToSize(Job startingPoint, int targetJobSize) throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        List<Job> foundChildrenJobs = new ArrayList<>();
        this.developBranchOfJobsFromJob(startingPoint);
        int childrenJobsSize = startingPoint.getSize() - 1;
        if( childrenJobsSize > 0 ) {
            foundChildrenJobs.addAll( getJobs(Action.GO, childrenJobsSize, null, null) );

            if( childrenJobsSize > targetJobSize ) {
                for (Job foundChildrenJob : foundChildrenJobs) {
                    List<Job> foundGrandChildrenJobs = developBranchOfJobsFromJobToSize(foundChildrenJob, targetJobSize);
                    if (!foundGrandChildrenJobs.isEmpty()) {
                        foundChildrenJobs = foundGrandChildrenJobs;
                        break;
                    }
                }
            }
        }

        return foundChildrenJobs;
    }

    List<Job> getJobsToDo_NoDevelop(int size, Integer limit, Integer offset) throws JobSizeException, JobRetrievalFailedException {
        return getJobs(Action.GO, size, limit, offset);
    }

    /**
     * Get Jobs To Do With Potential Development of jobs branches
     * @param size size of the requested jobs
     * @param limit maximum number of retrieved jobs
     * @param offset index of the first retrieved jobs (for pagination)
     * @return the list of available jobs to do
     * @throws JobDevelopmentFailedException thrown in case of branch-developing failure
     */
    public List<Job> getJobsToDo(int size, Integer limit, Integer offset) throws JobDevelopmentFailedException, JobSizeException, JobRetrievalFailedException {

        List<Job> foundJobs = getJobs(Action.GO, size, limit, offset);

        while( foundJobs.isEmpty() && size <= boardSize ) {

            List<Job> foundBiggerJobs = findJobsTodoGreaterThan(size, null, null);

            if (!foundBiggerJobs.isEmpty()) {
                for (Job biggerJob : foundBiggerJobs) {
                    List<Job> foundJobsUnlimited = developBranchOfJobsFromJobToSize(biggerJob, size);
                    if (!foundJobsUnlimited.isEmpty()) {
                        foundJobs = pagination( foundJobsUnlimited, limit, offset );
                        break;
                    }
                }
            }

            size++;
        }

        return foundJobs;
    }

    private List<Job> pagination(List<Job> foundJobsUnlimited, Integer limit, Integer offset) {

        return foundJobsUnlimited.stream()
                .skip(offset!=null? offset : 0)
                .limit(limit!=null? limit : Integer.MAX_VALUE)
                .collect(Collectors.toList());
    }

    /*
     * Will prune siblings Jobs if they are all DONE
     * Example:
     *   - Given 'A.B1.DONE' + 'A.B2.DONE'
     *   - When prune('A.B1.DONE')
     *   - Then creates 'A.DONE' (and removes 'A.B1.DONE' and 'A.B2.DONE')
     */
    @Transactional
    public void pruneJobs(Job job) throws JobPruneFailedException {
        if ( job!=null && job.isDone() ) {
            try {
                Node jobNode = nodeAdapter.fromJob(job);

                Optional<MaterializedPath> parent = searchTreeManager.getParent(jobNode.getPath());
                if( parent.isPresent() ) {
                    List<Node> siblings = searchTreeManager.getChildren(parent.get());

                    boolean allDone = siblings.stream()
                            .map( node -> node.getTag().equals(Action.DONE) )
                            .reduce(Boolean::logicalAnd).orElse(Boolean.FALSE);

                    boolean allSameSize = siblings.stream()
                            .map( node -> node.getPath().segmentsCount()==jobNode.getPath().segmentsCount() )
                            .reduce(Boolean::logicalAnd).orElse(Boolean.FALSE);

                    if ( !allDone ) {
                        LOGGER.debug("No pruning possible for " + jobNode.getPath().toString() + ": sub-tree is not all done");
                    } else {
                        if( !allSameSize ) {
                            LOGGER.debug("No pruning possible for " + jobNode.getPath().toString() + ": siblings have not the same size");
                        } else {
                            searchTreeManager.replacePath(siblings, Collections.singletonList(new Node(parent.get(), Action.DONE)));
                        }
                    }
                } else {
                    LOGGER.warn("The given job ("+jobNode.getPath()+") has no parent");
                }


            } catch( MaterializedPathReplaceFailedException e ) {
                throw new JobPruneFailedException( e.getErrors(), e );
            }
        }
    }

    /*
     * Will create sub-jobs from a given job
     * Example:
     *  - Given 'A.B1.GO'
     *  - Given children of 'A.B1' being C4,C6
     *  - When developBranchOfJobsFromJob('A.B1.GO')
     *  - Then creates 'A.B1.C4.GO', 'A.B1.C6.GO' (and removes 'A.B1.GO')
     */
    @Transactional
    public void developBranchOfJobsFromJob(Job startingPoint) throws JobDevelopmentFailedException {
        if( ! startingPoint.isDone() && ! startingPoint.isPending() ) {

            try {
                List<Job> subJobs = subJobsService.getSubJobs(startingPoint);

                if( subJobs.isEmpty() ) {

                    // No sub-job found: that means that the startingPoint job is DONE
                    Job jobDone = new Job( startingPoint, Action.DONE );
                    searchTreeManager.replacePath(Collections.singletonList(nodeAdapter.fromJob(startingPoint)), Collections.singletonList(nodeAdapter.fromJob(jobDone)));
                    this.pruneJobs(jobDone);

                } else {
                    List<Node> toAdd = subJobs.stream().map( nodeAdapter::fromJob ).collect(Collectors.toList());
                    searchTreeManager.replacePath(Collections.singletonList(nodeAdapter.fromJob(startingPoint)), toAdd);
                }

            } catch (SolverResultException e) {
                throw new JobDevelopmentFailedException(e.getErrors(), e);
            } catch (MaterializedPathReplaceFailedException e) {
                throw new JobDevelopmentFailedException(e.getErrors(), e);
            } catch (JobPruneFailedException e) {
                LOGGER.error("Job pruning failed for job "+startingPoint);
            } catch (Exception e2) {
                ErrorDescription unknownError = new ErrorDescription(INTERNAL_SERVER_ERROR, jobAdapter.toDescription(startingPoint).toString(), e2.getMessage());
                throw new JobDevelopmentFailedException(Collections.singletonList(unknownError), e2);
            }
        }
    }

    /*
     * Will replace a job "to do" with a job "done".
     * Example:
     *  - Given 'A.B1.GO'
     *  - When submitResult(empty or not) for job 'A.B1.GO'
     *  - Then remove 'A.B1.GO' and replace by new 'A.B1.DONE'
     */
    public void declareDone(Job initialJob) throws JobUpdateFailedException, JobPruneFailedException {

        try {
            Job doneJob = this.updateJobStatus(initialJob, Action.DONE);
            this.pruneJobs(doneJob);
        } catch (MaterializedPathUpdateFailedException e) {
            throw new JobUpdateFailedException(e.getError(), e);
        }
    }

    /*
     * Will replace a job "to do" with a job "pending".
     * Example:
     *  - Given 'A.B1.GO'
     *  - When declarePending('A.B1.GO')
     *  - Then remove 'A.B1.GO' and replace by new 'A.B1.PENDING'
     */
    public void declarePending(Job job) throws JobUpdateFailedException {

        try {
            this.updateJobStatus(job, Action.PENDING);
        } catch (MaterializedPathUpdateFailedException e1) {
            throw new JobUpdateFailedException(e1.getError(), e1);
        }
    }

    /*
     * Will replace a job "to do" with a job "done" (or "pending").
     * Example:
     *  - Given 'A.B1.GO'
     *  - When submitResult(empty or not) for job 'A.B1.GO'
     *  - Then remove 'A.B1.GO' and replace by new 'A.B1.DONE'
     */
    private Job updateJobStatus(Job initialJob, Action newStatus) throws MaterializedPathUpdateFailedException {

        Job jobDone = new Job( initialJob, newStatus );

        searchTreeManager.updateTag( nodeAdapter.fromJob(initialJob), newStatus );

        return jobDone;
    }


}
