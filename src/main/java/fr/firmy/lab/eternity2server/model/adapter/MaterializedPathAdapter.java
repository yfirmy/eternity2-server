package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.controller.exception.MalformedMaterializedPathException;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MaterializedPathAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger( MaterializedPathAdapter.class.getName() );
    private final int boardSize;
    private final BoardPath boardPath;
    private BoardDescriptionCheck boardDescriptionCheck;

    @Autowired
    public MaterializedPathAdapter(ServerConfiguration configuration, BoardPath boardPath, BoardDescriptionCheck boardDescriptionCheck) {
        this.boardSize = configuration.getBoardSize();
        this.boardPath = boardPath;
        this.boardDescriptionCheck = boardDescriptionCheck;
    }

    public BoardDescription toBoardDescription(MaterializedPath materializedPath) {

        String[] pieces = new String[boardSize];
        final int width = (int)Math.sqrt( boardSize );
        Arrays.fill(pieces, ".");
        for(int i = 0; i< materializedPath.segmentsCount(); i++) {
            String segment = materializedPath.getSegments().get(i);
            BoardPath.Coordinates coordinates = boardPath.get(i);
            pieces[ width * coordinates.y + coordinates.x ] = segment;
        }

        StringBuilder descriptionBuilder = new StringBuilder("$");
        List<String> preparedDescription = new ArrayList<>(boardSize);
        preparedDescription.addAll( Arrays.asList(pieces) );
        descriptionBuilder.append( preparedDescription.stream().collect(Collectors.joining(BoardDescription.separator())) );
        descriptionBuilder.append( BoardDescription.separator() + ";");

        return new BoardDescription(descriptionBuilder.toString());
    }

    public MaterializedPath fromBoardDescription(BoardDescription boardDescription) throws MalformedBoardDescriptionException {

        MaterializedPath result;

        boardDescriptionCheck.checkBoardDescriptionIsWellFormed(boardDescription);

        List<String> boardPieces = Arrays.asList(boardDescription.getRepresentation()
                .replaceFirst("\\$", "")
                .replaceFirst(";", "")
                .split(":"));

        final int width = (int)Math.sqrt( this.boardSize );
        String materializedPathStr = boardPath.stream()
                .map( coordinates -> boardPieces.get( coordinates.y * width + coordinates.x ) )
                .filter( s -> !s.equals(".") )
                .collect(Collectors.joining("."));

        try {
            result = new MaterializedPath(materializedPathStr);
        } catch(MalformedMaterializedPathException e) {
            // should not happen
            throw new MalformedBoardDescriptionException(boardDescription.getRepresentation(), e);
        }

        return result;

    }

}