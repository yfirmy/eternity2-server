package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import fr.firmy.lab.eternity2server.model.dto.SolverDescription;

import java.io.IOException;

public class SolverDescriptionDeserializer {

    public static SolverDescription deserialize(JsonNode solverDescriptionNode) throws IOException {
        SolverDescription result;

        JsonNode nameNode = solverDescriptionNode.get("name");
        JsonNode versionNode = solverDescriptionNode.get("version");
        JsonNode machineTypeNode = solverDescriptionNode.get("machineType");
        JsonNode clusterNameNode = solverDescriptionNode.get("clusterName");
        JsonNode scoreNode = solverDescriptionNode.get("score");

        if( nameNode != null ) {
            if( versionNode != null ) {
                if( machineTypeNode != null ) {
                    if( clusterNameNode != null ) {
                        if( scoreNode != null ) {

                            String name = nameNode.asText();
                            String version = versionNode.asText();
                            String machineType = machineTypeNode.asText();
                            String clusterName = clusterNameNode.asText();
                            String score = scoreNode.asText();

                            result = new SolverDescription( name, version, machineType, clusterName, score );

                        } else {
                            throw new IOException("Missing solver score in the SolverDescription");
                        }
                    } else {
                        throw new IOException("Missing cluster name in the SolverDescription");
                    }
                } else {
                    throw new IOException("Missing machine type in the SolverDescription");
                }
            } else {
                throw new IOException("Missing version in the SolverDescription");
            }
        } else {
            throw new IOException("Missing name in the SolverDescription");
        }

        return result;
    }
}
