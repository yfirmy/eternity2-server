package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.model.Board;
import fr.firmy.lab.eternity2server.model.Piece;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BoardAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger( BoardAdapter.class.getName() );

    public BoardAdapter() {
    }

    public BoardDescription toDescription(Board board) {
        BoardDescription boardDescription = null;
        StringBuilder boardRepresentation = new StringBuilder("#");
        for(int y=0; y<board.getBorderSize(); y++) {
            for(int x=0; x<board.getBorderSize(); x++) {
                Piece piece = board.get(x,y);
                boardRepresentation.append((piece!=null?piece.toString():".")+":");
            }
        }
        boardRepresentation.append(";");
        try {
            boardDescription = new BoardDescription(boardRepresentation.toString(), board.getSize());
        } catch (MalformedBoardDescriptionException e) {
            LOGGER.error("Should not happen", e);
        }
        return boardDescription;
    }

    public Board fromDescription(BoardDescription boardDescription) {
        Board board =  new Board(boardDescription.getBoardSize());

        String[] piecesStr = boardDescription.getRepresentation()
                .replaceFirst("#", "")
                .replaceFirst(";", "")
                .split(":");

        int idx = 0;
        int borderSize = board.getBorderSize();
        for( String pieceStr : piecesStr ) {
            Piece piece = null;
            if( ! pieceStr.equals(".") ) {
                piece = Piece.parsePiece(pieceStr);
            }
            board.set( idx%borderSize,  idx/borderSize, piece );
            idx++;
        }
        return board;
    }

}