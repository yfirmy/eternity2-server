package fr.firmy.lab.eternity2server.controller.dal;

public interface WorkersTimeline {

    void eventSolverStarted(String solver_name);

    void eventSolverSolving(String solver_name);

    void eventSolverIdle(String solver_name);

    void eventSolverStopped(String solver_name);
}
