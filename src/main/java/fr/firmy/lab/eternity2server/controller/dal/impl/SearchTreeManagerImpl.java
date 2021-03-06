package fr.firmy.lab.eternity2server.controller.dal.impl;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.dal.SearchTreeManager;
import fr.firmy.lab.eternity2server.model.*;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.controller.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Component
public class SearchTreeManagerImpl implements SearchTreeManager {

    private static Logger LOGGER = LoggerFactory.getLogger( SearchTreeManagerImpl.class.getName() );

    private static final String selectMatchingNodesRequest  =
            "SELECT path FROM search.tree WHERE path ~ ?::lquery AND tag = ?::search.action ORDER BY path ASC";

    private static final String selectExactNodesRequest  =
            "SELECT path FROM search.tree WHERE path = ?::ltree AND tag = ?::search.action ORDER BY path ASC";

    private static final String selectAllNodesRequest =
            "SELECT path, tag FROM search.tree WHERE path ~ ?::lquery ORDER BY path ASC";

    private static final String insertNodeRequest =
            "INSERT INTO search.tree (creation_time, path, tag) VALUES (DEFAULT, ?::ltree, ?::search.action)";

    private static final String removeNodeRequest =
            "DELETE FROM search.tree WHERE path = ?::ltree AND tag = ?::search.action";

    private static final String updateNodeStatus1 =
            "UPDATE search.tree SET tag = '%s' WHERE path = ?::ltree AND tag = '%s'";

    private static final String updateNodeStatus2 =
            "UPDATE search.tree SET tag = '%s' WHERE path = ?::ltree AND (tag = '%s' OR tag = '%s')";

    private final JdbcTemplate jdbcTemplate;
    private final int boardSize;

    private static Map<Action, String> updateNodeStatusRequests = new HashMap<>();

    static {
        updateNodeStatusRequests.put(Action.PENDING, String.format(updateNodeStatus1, Action.PENDING.name(), Action.GO.name()));
        updateNodeStatusRequests.put(Action.DONE, String.format(updateNodeStatus2, Action.DONE.name(), Action.PENDING.name(), Action.DONE.name()));
        updateNodeStatusRequests.put(Action.GO, String.format(updateNodeStatus1, Action.GO.name(), Action.PENDING.name()));
    }

    @Autowired
    public SearchTreeManagerImpl(JdbcTemplate jdbcTemplate, ServerConfiguration configuration) {
        this.jdbcTemplate = jdbcTemplate;
        this.boardSize = configuration.getBoardSize();
    }

    public List<MaterializedPath> getPathsAtLevel(Action tag, int level, Integer limit, Integer offset) {
        List<MaterializedPath> result = new ArrayList<>();
        if( level >= 0 && level <= boardSize ) {
            String lquery = "*{"+level+"}";
            result.addAll(getPaths(lquery, tag, limit, offset));
        }
        return result;
    }

    private List<MaterializedPath> getPaths(String lquery, Action tag, Integer limit, Integer offset) {
        String limitOption = limit!=null ? " LIMIT "+limit : "";
        String offsetOption = offset!=null ? " OFFSET "+offset : "";
        String selectQuery = ( lquery.isEmpty() ? selectExactNodesRequest : selectMatchingNodesRequest ) + limitOption + offsetOption;
        return jdbcTemplate.query(selectQuery, rs -> {
            List<MaterializedPath> result0 = new ArrayList<>();
            while (rs.next()) {
                Optional<MaterializedPath> materializedPath = MaterializedPath.build(rs.getString("path"));
                materializedPath.ifPresent(result0::add);
            }
            return result0;
        }, lquery, tag.name());
    }

    public List<Node> getAllPaths() {
        return this.getAllPaths("*");
    }

    private List<Node> getAllPaths(String lquery) {

        return jdbcTemplate.query(selectAllNodesRequest, rs -> {
            List<Node> result0 = new ArrayList<>();
            while (rs.next()) {
                String foundPath = rs.getString("path");
                String foundTag = rs.getString("tag");

                Optional<MaterializedPath> materializedPath = MaterializedPath.build(foundPath);
                materializedPath.ifPresent(path -> result0.add(new Node(path, Action.parseAction(foundTag))));
            }
            return result0;
        }, lquery);
    }

    public List<MaterializedPath> getDonePaths(int level) {
        return getPathsAtLevel(Action.DONE, level, null, null);
    }

    public List<MaterializedPath> getPendingPaths(int level) {
        return getPathsAtLevel(Action.PENDING, level, null, null);
    }

    public List<MaterializedPath> getPathsToDo(int level, Integer limit, Integer offset) {
        return getPathsAtLevel(Action.GO, level, limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void addPath(Node node) throws MaterializedPathAddFailedException {

        LOGGER.debug("Adding "+node.toString());

        try {
            jdbcTemplate.update(insertNodeRequest, node.getPath().toString(), node.getTag().name());
        } catch(Exception e) {
            LOGGER.error("Impossible to add Materialized Path " + node.getPath().toString());
            throw new MaterializedPathAddFailedException(node.getPath());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removePath(Node node) throws MaterializedPathRemoveFailedException {

        LOGGER.debug("Removing "+node.getPath().toString());

        try {
            int count = jdbcTemplate.update(removeNodeRequest, node.getPath().toString(), node.getTag().name());
            if( count == 0 ) {
                LOGGER.error("Impossible to remove Materialized Path " + node.getPath().toString());
                throw new MaterializedPathRemoveFailedException(node.getPath());
            }
        } catch(Exception e) {
            LOGGER.error("Impossible to remove Materialized Path " + node.getPath().toString());
            throw new MaterializedPathRemoveFailedException(node.getPath());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateTag(Node node, Action newTag) throws MaterializedPathUpdateFailedException {

        LOGGER.info("Setting tag "+newTag+ " to "+node.getPath().toString());

        try {
            int count = jdbcTemplate.update(updateNodeStatusRequests.get(newTag), node.getPath().toString());
            if( count == 0 ) {
                ErrorDescription error = new ErrorDescription(BAD_REQUEST, node.getPath().toString(), String.format("Status cannot been updated for Materialized Path (%s)", node.getPath().toString()));
                LOGGER.error(error.getMessage());
                throw new MaterializedPathUpdateFailedException(node.getPath(), error);
            }
        } catch(MaterializedPathUpdateFailedException e) {
            throw e;
        } catch(Exception e) {
            ErrorDescription error = new ErrorDescription(INTERNAL_SERVER_ERROR, node.getPath().toString(), String.format("Impossible to update status for Materialized Path (%s)", node.getPath().toString()));
            LOGGER.error(error.getMessage());
            throw new MaterializedPathUpdateFailedException(node.getPath(), error, e);
        }
    }

    private boolean existsNode(Node node) {
        return this.getPaths( node.getPath().toString(), node.getTag(), null, null ).size()>0 ;
    }

    public List<Node> getChildren(MaterializedPath materializedPath) {
        return getAllPaths(  ( materializedPath.isRoot() ? "" : materializedPath.toString() + "." ) + "*" ).stream()
                .filter(line -> !line.getPath().equals(materializedPath))
                .collect(Collectors.toList());
    }

    public Optional<MaterializedPath> getParent(MaterializedPath materializedPath) {
        return materializedPath.getParent();
    }

    public boolean isParentOf (MaterializedPath parent, MaterializedPath child) {
        Optional<MaterializedPath> actualParent = this.getParent(child);
        return (actualParent.isPresent() && parent.equals( actualParent.get() )) || ( !actualParent.isPresent() && parent.isRoot() );
    }

    public boolean isAncestorOf(MaterializedPath grandparent, MaterializedPath child) {
        Optional<MaterializedPath> actualParent = this.getParent(child);
        while( actualParent.isPresent() && !actualParent.get().equals(grandparent) ) {
            actualParent = actualParent.get().getParent();
        }
        return (actualParent.isPresent() && grandparent.equals( actualParent.get() )) || ( !actualParent.isPresent() && grandparent.isRoot() );
    }

    // TODO : Test potential transaction rollbacks !!
    @Transactional
    public void replacePath(List<Node> listToRemove, List<Node> listToAdd) throws MaterializedPathReplaceFailedException {

        LOGGER.info("Replacing "+listToRemove.stream().map(Node::toString).collect(Collectors.joining(", "))+" by "+listToAdd.stream().map(Node::toString).collect(Collectors.joining(", ")));

        List<ErrorDescription> errors = new ArrayList<>();

        for( Node pathToRemove : listToRemove ) {

            try {
                if (!listToAdd.contains(pathToRemove)) {

                    if (this.existsNode(pathToRemove)) {
                        try {
                            this.removePath(pathToRemove);
                        } catch (MaterializedPathRemoveFailedException e) {
                            addError(errors, INTERNAL_SERVER_ERROR, pathToRemove.toString(), "the removal of the given path has failed: " + e.getMessage());
                        }
                    } else {
                        addError(errors, BAD_REQUEST, pathToRemove.toString(), "the given job does not exist");
                    }
                } else {
                    addError(errors, BAD_REQUEST, pathToRemove.toString(), "it is also present in the requested paths to be inserted");
                }

            } catch (Exception e) {
                addError(errors, INTERNAL_SERVER_ERROR, pathToRemove.toString(), "of an unexpected failure" );
            }
        }

        if( errors.isEmpty() ) {
            for (Node pathToAdd : listToAdd) {
                try {
                    this.addPath(pathToAdd);
                } catch (MaterializedPathAddFailedException e) {
                    addError(errors, INTERNAL_SERVER_ERROR, pathToAdd.toString(), "the insertion of the given path has failed: "+e.getMessage());
                }
            }
        }

        if( !errors.isEmpty() ) {
            throw new MaterializedPathReplaceFailedException(errors);
        }
    }

    private void addError(List<ErrorDescription> errors, HttpStatus httpStatus, String path, String cause) {
        String errorMsg = "Impossible to replace the given Materialized Path, because "+cause+ " ("+path+")";
        LOGGER.error(errorMsg);
        errors.add( new ErrorDescription(httpStatus, path, errorMsg) );
    }

}
