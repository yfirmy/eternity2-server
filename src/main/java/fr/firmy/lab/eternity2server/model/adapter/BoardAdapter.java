package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.model.Board;
import fr.firmy.lab.eternity2server.model.Piece;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger( BoardAdapter.class.getName() );

    private BoardDescriptionCheck boardDescriptionCheck;

    @Autowired
    public BoardAdapter(BoardDescriptionCheck boardDescriptionCheck) {
        this.boardDescriptionCheck = boardDescriptionCheck;
    }

    public BoardDescription toDescription(Board board) {

        StringBuilder boardRepresentation = new StringBuilder("$");
        for(int y=0; y<board.getBorderSize(); y++) {
            for(int x=0; x<board.getBorderSize(); x++) {
                Piece piece = board.get(x,y);
                boardRepresentation.append((piece!=null?piece.toString():".")+":");
            }
        }
        boardRepresentation.append(";");

        return new BoardDescription(boardRepresentation.toString());
    }

    public Board fromDescription(BoardDescription boardDescription) throws MalformedBoardDescriptionException {

        boardDescriptionCheck.checkBoardDescriptionIsWellFormed(boardDescription);

        Board board =  new Board(boardDescriptionCheck.getBoardSize());

        String[] piecesStr = boardDescription.getRepresentation()
                .replaceFirst("\\$", "")
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