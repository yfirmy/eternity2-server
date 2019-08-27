package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class JobDescriptionSerializer extends JsonSerializer<JobDescription> {

    @Override
    public void serialize(JobDescription jobDescription, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("job", jobDescription.getJob().getRepresentation());
        jsonGenerator.writeEndObject();
    }
}
