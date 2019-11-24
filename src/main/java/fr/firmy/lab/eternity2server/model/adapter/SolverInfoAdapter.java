package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.controller.exception.MalformedSolverDescriptionException;
import fr.firmy.lab.eternity2server.model.SolverInfo;
import fr.firmy.lab.eternity2server.model.dto.SolverDescription;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class SolverInfoAdapter {

    public SolverInfo fromDescription(SolverDescription description, String solverIpAddress) throws MalformedSolverDescriptionException {

        SolverInfo result;
        try {
            result = new SolverInfo(
                    description.getName(),
                    InetAddress.getByName(solverIpAddress),
                    description.getVersion(),
                    description.getMachineType(),
                    description.getClusterName(),
                    Double.parseDouble(description.getScore())
            );
        } catch (UnknownHostException e) {
            throw new MalformedSolverDescriptionException(solverIpAddress);
        } catch (NumberFormatException e) {
            throw new MalformedSolverDescriptionException(description.getScore());
        }
        return result;
    }
}
