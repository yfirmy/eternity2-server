package fr.firmy.lab.eternity2server.model.dto;

import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;

public class BoardDescription {

    private String representation;
    private Integer boardSize;

    private static final String separator = ":";
    private static final String boardPiecePattern = "(\\d{1,3}[WNES])|\\.";

    public BoardDescription() {}

    public BoardDescription(String representation, int boardSize) throws MalformedBoardDescriptionException {
        if( representation==null || ! representation.matches(boardDescriptionPattern(boardSize)) ) {
            throw new MalformedBoardDescriptionException(representation);
        }
        this.representation = representation;
        this.boardSize = boardSize;
    }

    private static String boardDescriptionPattern(int boardSize) {
        return "#(("+boardPiecePattern+")\\"+separator+"){"+boardSize+"};";
    }

    public static CharSequence separator() {
        return separator;
    }

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {
        return this.representation;
    }

    public int getBoardSize() {
        return this.boardSize;
    }
}
