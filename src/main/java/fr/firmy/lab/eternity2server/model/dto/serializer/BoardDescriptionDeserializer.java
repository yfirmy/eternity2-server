package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.exception.BoardDescriptionParseException;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class BoardDescriptionDeserializer extends JsonDeserializer<BoardDescription> {

    private final int boardSize;

    @Autowired
    public BoardDescriptionDeserializer(ServerConfiguration configuration) {
        this.boardSize = configuration.getBoardSize();
    }

    @Override
    public BoardDescription deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        BoardDescription boardDescription;
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        String description = node.asText();
        try {
            boardDescription = new BoardDescription(description, boardSize);
        } catch (MalformedBoardDescriptionException e) {
            throw new BoardDescriptionParseException(jsonParser, description , e);
        }
        return boardDescription;
    }
}
