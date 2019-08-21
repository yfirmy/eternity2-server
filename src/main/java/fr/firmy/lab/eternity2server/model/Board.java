package fr.firmy.lab.eternity2server.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Board {

    private static Logger LOGGER = LoggerFactory.getLogger( Board.class );

    private Piece[][] pieces;
    private int size;
    private int borderSize;
    private int piecesCount;

    public int getSize() {
        return this.size;
    }

    public int getBorderSize() {
        return this.borderSize;
    }

    public int getPiecesCount() {
        return this.piecesCount;
    }

    public Board(int boardSize) {
        this.size = boardSize;
        this.borderSize = (int)Math.sqrt(boardSize);
        pieces = new Piece[borderSize][borderSize];
    }

    public Piece get(int x, int y) {
        return this.pieces[y][x];
    }

    public void set(int x, int y, Piece piece) {
        this.pieces[y][x] = piece;
        this.updatePiecesCount();
    }

    private void updatePiecesCount() {
        int count = 0;
        for(Piece[] line : pieces) {
            for(Piece piece : line) {
                if( piece!=null ) count++;
            }
        }
        this.piecesCount = count;
    }
}
