package fr.firmy.lab.eternity2server.model.dto.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import fr.firmy.lab.eternity2server.model.dto.JobDescription;
import fr.firmy.lab.eternity2server.model.dto.ResultDescription;
import fr.firmy.lab.eternity2server.model.dto.SolutionDescription;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@JsonComponent
public class ResultDescriptionDeserializer extends JsonDeserializer<ResultDescription> {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public ResultDescription deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        ResultDescription result;

        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        JsonNode jobNode = node.get("job");
        JsonNode solutionsNode = node.get("solutions");

        if( jobNode != null ) {
            if( solutionsNode != null ) {

                String boardDescription = jobNode.asText();
                Iterator<JsonNode> itSolutions = solutionsNode.iterator();
                List<SolutionDescription> solutions = new ArrayList<>();

                while (itSolutions.hasNext()) {
                    JsonNode solutionNode = itSolutions.next();
                    try {
                        String optionalDate = solutionNode.get("dateSolved").asText();
                        solutions.add(new SolutionDescription(
                                new BoardDescription(solutionNode.get("solution").asText()),
                                optionalDate.isEmpty() ? new Date() : dateFormat.parse(optionalDate)));
                    } catch (ParseException e) {
                        throw new IOException("Impossible to parse dateSolved field", e);
                    }
                }
                String strDateJobTransmission = node.get("dateJobTransmission").asText();

                try {
                    result = new ResultDescription(
                            new JobDescription(new BoardDescription(boardDescription)),
                            solutions,
                            strDateJobTransmission.isEmpty() ? new Date() : dateFormat.parse(strDateJobTransmission));
                } catch (ParseException e) {
                    throw new IOException("Impossible to parse dateJobTransmission field", e);
                }
            } else {
                throw new IOException("Missing solutions in the ResultDescription");
            }
        } else {
            throw new IOException("Missing job in the ResultDescription");
        }

        return result;
    }
}
