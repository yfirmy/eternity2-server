package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardDescriptionCheck {

    private static final String boardPiecePattern = "(\\d{1,3}[WNES])|\\.";
    private final Integer boardSize;

    @Autowired
    public BoardDescriptionCheck(ServerConfiguration serverConfiguration) {
        this.boardSize = serverConfiguration.getBoardSize();
    }

    public void checkBoardDescriptionIsWellFormed(BoardDescription boardDescription) throws MalformedBoardDescriptionException {
        if( boardDescription.getRepresentation()==null || ! boardDescription.getRepresentation().matches(boardDescriptionPattern(boardSize)) ) {
            throw new MalformedBoardDescriptionException(boardDescription.getRepresentation());
        }
    }

    private static String boardDescriptionPattern(int boardSize) {
        return "\\$(("+boardPiecePattern+")\\"+BoardDescription.separator()+"){"+boardSize+"};";
    }

    public int getBoardSize() {
        return this.boardSize;
    }
}
