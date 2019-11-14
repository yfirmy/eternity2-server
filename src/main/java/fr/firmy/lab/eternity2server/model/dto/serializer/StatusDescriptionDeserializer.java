package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import fr.firmy.lab.eternity2server.model.dto.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonComponent
public class StatusDescriptionDeserializer extends JsonDeserializer<StatusDescription> {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public StatusDescription deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        StatusDescription result;
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        JsonNode jobNode = node.get("job");
        JsonNode statusNode = node.get("status");
        JsonNode dateJobTransmissionNode = node.get("dateJobTransmission");
        JsonNode dateStatusUpdateNode = node.get("dateStatusUpdate");

        if( jobNode != null ) {
            if( statusNode != null ) {
                if( dateJobTransmissionNode != null ) {
                    if( dateStatusUpdateNode != null ) {

                        String boardDescription = jobNode.asText();
                        String status = statusNode.asText();
                        String strDateJobTransmission = dateJobTransmissionNode.asText();
                        String strDateStatusUpdate = dateStatusUpdateNode.asText();

                        Date dateJobTransmission;
                        Date dateStatusUpdate;

                        try {
                            dateJobTransmission = strDateJobTransmission.isEmpty() ? new Date() : dateFormat.parse(strDateJobTransmission);
                        } catch (ParseException e) {
                            throw new IOException("Impossible to parse dateJobTransmission field", e);
                        }

                        try {
                            dateStatusUpdate = strDateStatusUpdate.isEmpty() ? new Date() : dateFormat.parse(strDateStatusUpdate);
                        } catch (ParseException e) {
                            throw new IOException("Impossible to parse dateStatusUpdate field", e);
                        }

                        result = new StatusDescription(
                                new JobDescription(new BoardDescription(boardDescription)),
                                status,
                                dateJobTransmission,
                                dateStatusUpdate
                        );

                    } else {
                        throw new IOException("Missing dateStatusUpdate in the StatusDescription");
                    }
                } else {
                    throw new IOException("Missing dateJobTransmission in the StatusDescription");
                }
            } else {
                throw new IOException("Missing status in the StatusDescription");
            }
        } else {
            throw new IOException("Missing job in the StatusDescription");
        }

        return result;
    }
}
