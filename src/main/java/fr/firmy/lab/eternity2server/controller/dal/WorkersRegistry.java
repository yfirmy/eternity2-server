package fr.firmy.lab.eternity2server.controller.dal;

import fr.firmy.lab.eternity2server.controller.exception.RegisteringFailedException;
import fr.firmy.lab.eternity2server.controller.exception.UnregisteringFailedException;
import fr.firmy.lab.eternity2server.model.Node;

import java.util.List;

public interface WorkersRegistry {

    void addPendingJob(Node pending_job,
                       String solver_name,
                       String solver_ip,
                       String solver_version,
                       String solver_machine_type,
                       String solver_cluster_name,
                       Double solver_score) throws RegisteringFailedException;

    void removePendingJob(Node pending_job,
                          String solver_name) throws UnregisteringFailedException;

    List<Node> getPendingJobs(String solver_name,
                              String solver_ip,
                              String solver_version,
                              String solver_machine_type,
                              String cluster_name,
                              Double solver_score);
}
