package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class JobDescriptionDeserializer extends JsonDeserializer<JobDescription> {

    @Override
    public JobDescription deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        String description = node.get("job").asText();
        return new JobDescription(new BoardDescription(description));
    }
}
