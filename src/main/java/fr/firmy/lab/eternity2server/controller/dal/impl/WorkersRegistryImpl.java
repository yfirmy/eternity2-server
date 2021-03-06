package fr.firmy.lab.eternity2server.controller.dal.impl;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.dal.WorkersRegistry;
import fr.firmy.lab.eternity2server.controller.exception.*;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Node;
import fr.firmy.lab.eternity2server.model.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class WorkersRegistryImpl implements WorkersRegistry {

    private static Logger LOGGER = LoggerFactory.getLogger(WorkersRegistryImpl.class.getName());

    private static final String addPendingJobRequest =
            "INSERT INTO search.pending "+
            "(pending_job, solver_name, solver_ip, solver_version, solver_machine_type, solver_cluster_name, solver_score, solving_start_time) "+
            "VALUES (?::ltree, ?, ?::inet, ?, ?, ?, ?, DEFAULT)";

    private static final String removePendingJobRequest =
            "DELETE FROM search.pending "+
            "WHERE pending_job = ?::ltree AND solver_name = ?";

    private static final String selectPendingJobRequest =
            "SELECT pending_job FROM search.pending "+
            "WHERE solver_name = ? "+
                "AND solver_ip = ?::inet "+
                "AND solver_version = ? "+
                "AND solver_machine_type = ? "+
                "AND solver_cluster_name = ? "+
                "AND solver_score = ?::double precision "+
            "ORDER BY solving_start_time ASC";

    private final JdbcTemplate jdbcTemplate;
    private final int boardSize;

    @Autowired
    public WorkersRegistryImpl(JdbcTemplate jdbcTemplate, ServerConfiguration configuration) {
        this.jdbcTemplate = jdbcTemplate;
        this.boardSize = configuration.getBoardSize();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void addPendingJob(Node pending_job,
                              String solver_name,
                              String solver_ip,
                              String solver_version,
                              String solver_machine_type,
                              String solver_cluster_name,
                              Double solver_score) throws RegisteringFailedException {

        LOGGER.debug("Registering solver "+solver_name+"("+solver_cluster_name+")["+solver_ip+"] solving job "+pending_job.getPath().toString());

        try {
            jdbcTemplate.update(addPendingJobRequest,
                    pending_job.getPath().toString(),
                    solver_name,
                    solver_ip,
                    solver_version,
                    solver_machine_type,
                    solver_cluster_name,
                    solver_score);

        } catch(Exception e) {
            throw new RegisteringFailedException(solver_name, pending_job, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removePendingJob(Node pending_job,
                              String solver_name) throws UnregisteringFailedException {

        LOGGER.debug("Unregistering solver "+solver_name + " solving job "+pending_job.getPath().toString());

        try {
            int count = jdbcTemplate.update(removePendingJobRequest, pending_job.getPath().toString(), solver_name);
            if( count == 0 ) {
                LOGGER.warn("Pending job "+pending_job.getPath().toString()+" associated with solver "+solver_name+" was not found in Registry");
            }
        } catch(Exception e) {
            throw new UnregisteringFailedException(solver_name, pending_job, e);
        }
    }

    @Override
    public List<Node> getPendingJobs(String solver_name,
                              String solver_ip,
                              String solver_version,
                              String solver_machine_type,
                              String solver_cluster_name,
                              Double solver_score) {

        return jdbcTemplate.query(selectPendingJobRequest, rs -> {
            List<Node> pendingJobs = new ArrayList<>();
            while (rs.next()) {
                Optional<MaterializedPath> materializedPath = MaterializedPath.build(rs.getString("pending_job"));
                materializedPath.ifPresent(path -> pendingJobs.add(new Node(path, Action.PENDING)));
            }
            return pendingJobs;
            }, solver_name, solver_ip, solver_version, solver_machine_type, solver_cluster_name, solver_score
        );
    }

}
