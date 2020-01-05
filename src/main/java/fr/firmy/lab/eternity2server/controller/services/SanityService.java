package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.dal.SearchTreeManager;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.controller.exception.MalformedJobDescriptionException;
import fr.firmy.lab.eternity2server.controller.exception.SolverResultException;
import fr.firmy.lab.eternity2server.controller.exception.TreeSanityCheckFailedException;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.Job;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Node;
import fr.firmy.lab.eternity2server.model.adapter.JobAdapter;
import fr.firmy.lab.eternity2server.model.adapter.MaterializedPathAdapter;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import fr.firmy.lab.eternity2server.model.dto.FailedCheckDescription;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SanityService {

    private static Logger LOGGER = LoggerFactory.getLogger(SanityService.class);

    private final SearchTreeManager tree;
    private final SubJobsService backend;

    // adapters
    private JobAdapter jobAdapter;
    private MaterializedPathAdapter materializedPathAdapter;

    private final TreeMap<MaterializedPath, Action> cacheDatabase = new TreeMap<>();
    private final TreeMap<MaterializedPath, List<MaterializedPath>> cacheBackend = new TreeMap<>();
    private final TreeMap<MaterializedPath, List<MaterializedPath>> childrenCache = new TreeMap<>();
    private final TreeMap<MaterializedPath, Set<MaterializedPath>> siblingsCache = new TreeMap<>();

    @Autowired
    public SanityService(SearchTreeManager searchTreeManager, SubJobsService subJobsService, ServerConfiguration configuration,
                         JobAdapter jobAdapter, MaterializedPathAdapter materializedPathAdapter) {

        this.tree = searchTreeManager;
        this.backend = subJobsService;
        this.jobAdapter = jobAdapter;
        this.materializedPathAdapter = materializedPathAdapter;
    }

    private void load(List<Node> lines) {
        for( Node node : lines ) {
            cacheDatabase.put( node.getPath(), node.getTag() );
        }
    }

    private void flushCaches() {
        cacheDatabase.clear();
        childrenCache.clear();
        siblingsCache.clear();
    }

    public void check() throws TreeSanityCheckFailedException {

        List<FailedCheckDescription> errors = new ArrayList<>();

        this.load(tree.getAllPaths());
        int progress = 0;

        for (MaterializedPath path : cacheDatabase.navigableKeySet()) {
            progress++;

            Action tag = cacheDatabase.get(path);

            switch (tag) {
                case GO:
                    errors.addAll( checkJobToDo( path ) );
                    break;
                case PENDING:
                    errors.addAll( checkJobPending( path ));
                    break;
                case DONE:
                    errors.addAll( checkJobDone( path ));
                    break;
            }

            if( errors.isEmpty() ) {
                LOGGER.info( "("+progress+"/"+ cacheDatabase.size()+") [OK] " + path.toString() );
            } else {
                LOGGER.error( "("+progress+"/"+ cacheDatabase.size()+") [FAILED]" + path.toString() );
            }
        }

        if( !errors.isEmpty() ) {
            throw new TreeSanityCheckFailedException(errors);
        }

        flushCaches();
    }

    private List<FailedCheckDescription> checkJobToDo(MaterializedPath path) {

        List<FailedCheckDescription> errors = new ArrayList<>();

        // pas de descendant
        pathHasNoChildInTree( path ) .ifPresent( errors::add );

        // pas d'ancetre en ligne directe en base (sur chacune des générations)
        pathHasNoAncestorInTree( path ) .ifPresent( errors::add );

        return errors;
    }

    private List<FailedCheckDescription> checkJobPending(MaterializedPath path) {

        List<FailedCheckDescription> errors = new ArrayList<>();

        // pas de descendant
        pathHasNoChildInTree( path ) .ifPresent( errors::add );

        // pas d'ancetre en ligne directe en base (sur chacune des générations)
        pathHasNoAncestorInTree( path ) .ifPresent( errors::add );

        return errors;
    }

    private List<FailedCheckDescription> checkJobDone(MaterializedPath path) {

        List<FailedCheckDescription> errors = new ArrayList<>();

        // pas de descendant
        pathHasNoChildInTree( path ) .ifPresent( errors::add );

        // pas d'ancetre en ligne directe en base (sur chacune des générations)
        pathHasNoAncestorInTree( path ) .ifPresent( errors::add );

        // il existe des jobs frères en base
        // parmi les frères en base, au moins un a son tag pas-DONE
        pathHasSiblingsNotDoneInTree( path ) .ifPresent( errors::add );

        // à chacune des générations qui le précède :
        // - du père, seuls les enfants présents en base sont exactement les possibilités données par le backend subjobs (strict égalité - pas d'enfant manquant ou en trop)
        pathParentConsistency( path.getParent() ) .ifPresent( errors::add );

        return errors;
    }

    private List<MaterializedPath> subJobs( MaterializedPath parent ) throws SolverResultException, MalformedBoardDescriptionException, MalformedJobDescriptionException {

        if( !cacheBackend.containsKey( parent ) ) {
            Job parentJob = jobAdapter.fromDescription( new JobDescription( materializedPathAdapter.toBoardDescription(parent) ), Action.GO  );

            List<Job> results = this.backend.getSubJobs(parentJob);
            List<BoardDescription> boardDescriptions = results.stream()
                    .map(jobAdapter::toDescription)
                    .map(JobDescription::getJob).collect(Collectors.toList());
            List<MaterializedPath> translatedResults = new ArrayList<>();

            for( BoardDescription boardDescription : boardDescriptions ) {
                translatedResults.add( materializedPathAdapter.fromBoardDescription(boardDescription) );
            }

            cacheBackend.put(parent, translatedResults);
        }
        return cacheBackend.get( parent );
    }

    private List<MaterializedPath> foundChildrenInCache( MaterializedPath parent ) {

        if( !childrenCache.containsKey(parent) ) {

            TreeSet<MaterializedPath> children = new TreeSet<>();
            for (MaterializedPath candidate : cacheDatabase.subMap(parent, false, cacheDatabase.lastKey(), true).navigableKeySet()) {
                if (tree.isAncestorOf(parent, candidate)) {
                    Optional<MaterializedPath> childAncestor = candidate.getAncestor(parent.segmentsCount() + 1);
                    children.add(childAncestor.get());
                } else {
                    break;
                }
            }
            childrenCache.put(parent, new ArrayList(children));
        }

        return childrenCache.get(parent);
    }

    private Optional<FailedCheckDescription> pathParentConsistency(Optional<MaterializedPath> parent) {

        if( parent.isPresent() ) {

            try {
                List<MaterializedPath> expectedChildren = subJobs(parent.get());
                List<MaterializedPath> foundChildren = foundChildrenInCache(parent.get());

                if (!foundChildren.containsAll(expectedChildren)) {
                    return Optional.of(new FailedCheckDescription("Missing children in database", parent.toString()));
                }
                if (!expectedChildren.containsAll(foundChildren)) {
                    return Optional.of(new FailedCheckDescription("Unexpected children in database", parent.toString()));
                }
            }
            catch (SolverResultException | MalformedJobDescriptionException | MalformedBoardDescriptionException e) {
                return Optional.of(new FailedCheckDescription("Impossible to check parent/children consistency", parent.toString()));
            }
        }
        return Optional.empty();
    }

    private Optional<FailedCheckDescription> pathHasNoChildInTree(MaterializedPath path) {
        if( !tree.getChildren(path).isEmpty() ) {
            return Optional.of( new FailedCheckDescription("Materialized Path has children in database", path.toString()) );
        }
        return Optional.empty();
    }

    private Optional<FailedCheckDescription> pathHasNoAncestorInTree(MaterializedPath path) {
        Optional<MaterializedPath> parent = path.getParent();
        while (parent.isPresent() && !parent.get().isRoot()) {
            if ( cacheDatabase.containsKey( parent.get() ) ) {
                return Optional.of(new FailedCheckDescription("Materialized Path has one parent in database", path.toString()));
            }
            parent = parent.get().getParent();
        }
        return Optional.empty();
    }

    private Optional<FailedCheckDescription> pathHasSiblingsNotDoneInTree(MaterializedPath path) {
        int siblingsCount = this.getSiblingsCount(path);
        int siblingsDoneCount = this.getSiblingsDoneCount(path);

        if( siblingsCount == 0 ) {
            return Optional.of( new FailedCheckDescription( "Materialized Path is DONE without siblings in database", path.toString() ));
        }

        if( siblingsCount == siblingsDoneCount ) {
            return Optional.of( new FailedCheckDescription( "Materialized Path is DONE with all siblings DONE in database", path.toString() ));
        }

        return Optional.empty();
    }

    private Set<MaterializedPath> getSiblingsFamily(MaterializedPath path) {

        Optional<MaterializedPath> parent = path.getParent();
        TreeSet<MaterializedPath> results = new TreeSet<>();
        if( parent.isPresent() ) {

            if( !siblingsCache.containsKey(parent.get()) ) {

                TreeSet<MaterializedPath> siblings = new TreeSet<>();
                for (MaterializedPath candidate : cacheDatabase.subMap(parent.get(), false, cacheDatabase.lastKey(), true).navigableKeySet()) {
                    if (tree.isAncestorOf(parent.get(), candidate) && !candidate.equals(path)) {
                        siblings.add(candidate);
                    } else {
                        if (!candidate.equals(path)) {
                            break;
                        }
                    }
                }

                siblingsCache.put(parent.get(), siblings);
            }
            results.addAll(siblingsCache.get(parent.get()));
        }
        return results;
    }

    private int getSiblingsCount(MaterializedPath path) {
        return getSiblingsFamily(path).size();
    }

    private int getSiblingsDoneCount(MaterializedPath path) {
        return (int) getSiblingsFamily(path).stream().map(cacheDatabase::get).filter(o -> o.equals(Action.DONE)).count();
    }
}
