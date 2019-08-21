package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class BoardDescriptionSerializer extends JsonSerializer<BoardDescription> {

    @Override
    public void serialize(BoardDescription boardDescription, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(boardDescription.getRepresentation());
    }
}
