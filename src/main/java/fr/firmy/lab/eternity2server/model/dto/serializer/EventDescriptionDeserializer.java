package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import fr.firmy.lab.eternity2server.model.dto.EventDescription;

import java.io.IOException;

public class EventDescriptionDeserializer {

    public static EventDescription deserialize(JsonNode solverDescriptionNode) throws IOException {
        EventDescription result;

        JsonNode solverNameNode = solverDescriptionNode.get("solverName");
        JsonNode statusNode = solverDescriptionNode.get("status");

        if( solverNameNode != null ) {
            if( statusNode != null ) {
                String solverName = solverNameNode.asText();
                String status = statusNode.asText();
                result = new EventDescription( solverName, status );
            } else {
                throw new IOException("Missing status in the EventDescription");
            }
        } else {
            throw new IOException("Missing solverName in the EventDescription");
        }

        return result;
    }
}
