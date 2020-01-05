package fr.firmy.lab.eternity2server.controller.dal.impl;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.dal.SolutionsRepository;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Solution;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import fr.firmy.lab.eternity2server.controller.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Component
public class SolutionsRepositoryImpl implements SolutionsRepository {

    private static Logger LOGGER = LoggerFactory.getLogger( SolutionsRepositoryImpl.class.getName() );

    private static final String selectResultsRequest =
            "SELECT path, creation_time FROM search.results ORDER BY creation_time ASC";

    private static final String addResultRequest =
            "INSERT INTO search.results (creation_time, path) VALUES (DEFAULT, ?::ltree)";

    private final JdbcTemplate jdbcTemplate;
    private final int boardSize;

    @Autowired
    public SolutionsRepositoryImpl(JdbcTemplate jdbcTemplate, ServerConfiguration configuration) {
        this.jdbcTemplate = jdbcTemplate;
        this.boardSize = configuration.getBoardSize();
    }

    public List<Solution> getSolutions(Integer limit, Integer offset) {
        String limitOption = limit!=null ? " LIMIT "+ limit : "";
        String offsetOption = offset!=null ? " OFFSET "+ offset : "";
        String selectQuery = selectResultsRequest + limitOption + offsetOption;
        return jdbcTemplate.query(selectQuery, rs -> {
            List<Solution> solutions = new ArrayList<>();
            while (rs.next()) {
                Optional<MaterializedPath> materializedPath = MaterializedPath.build(rs.getString("path"));
                Date creationDate = rs.getDate("creation_time");

                if (materializedPath.isPresent()) {
                    Solution solution = new Solution(materializedPath.get(), creationDate);
                    solutions.add(solution);
                }
            }
            return solutions;
        });
    }

    public void addSolutions(List<Solution> solutions) throws ResultSubmissionFailedException {

        List<ErrorDescription> errors = new ArrayList<>();

        if( solutions != null ) {

            for( Solution solution : solutions ) {

                Optional<ErrorDescription> error = Optional.empty();

                if ( solution.getPath().segmentsCount() == boardSize ) {

                    try {
                        jdbcTemplate.update(addResultRequest, solution.getPath().toString());
                    } catch (DuplicateKeyException e) {
                        String errorMessage = "The given putResult already exists in the tree : "+e.getMessage();
                        error = Optional.of( new ErrorDescription( CONFLICT, solution.toString(), errorMessage ));
                    } catch (Exception e) {
                        String errorMessage = "An unexpected error occurred during the given putResult submission : "+e.getMessage();
                        error = Optional.of( new ErrorDescription( INTERNAL_SERVER_ERROR, solution.toString(), errorMessage ));
                    }

                } else {

                    String errorMessage = "The given putResult is a malformed materialized path (incorrect size for a finished job)";
                    error = Optional.of( new ErrorDescription( BAD_REQUEST, solution.toString(), errorMessage ));
                }

                error.ifPresent(errors::add);
            }
        } else {
            errors.add( new ErrorDescription(BAD_REQUEST, null, "Solutions are missing in the request") );
        }

        if( ! errors.isEmpty() ) {
            throw new ResultSubmissionFailedException( errors );
        }
    }


}
