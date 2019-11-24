package fr.firmy.lab.eternity2server.controller.exception;

import fr.firmy.lab.eternity2server.model.Node;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import org.springframework.http.HttpStatus;

import java.util.List;

public class RegisteringFailedException extends AbstractSingleErrorException {

    public RegisteringFailedException(String solver_name, Node pending_job) {
        super("Impossible to register the solver "+solver_name+" for the pending job "+pending_job.getPath().toString(),
                new ErrorDescription(HttpStatus.INTERNAL_SERVER_ERROR, solver_name, "The registering failed for solver "+ solver_name +" on job "+pending_job.getPath().toString()));
    }

    public RegisteringFailedException(String solver_name, Node pending_job, Throwable cause) {
        super("Impossible to register the solver "+solver_name+" for the pending job "+pending_job.getPath().toString(),
                new ErrorDescription(HttpStatus.INTERNAL_SERVER_ERROR, solver_name, "The registering failed for solver "+ solver_name +" on job "+pending_job.getPath().toString()), cause);
    }

}
