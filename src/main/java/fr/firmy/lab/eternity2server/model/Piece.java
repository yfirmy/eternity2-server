package fr.firmy.lab.eternity2server.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Piece {

    private int no;
    private Direction direction;

    private static Pattern segmentPattern = Pattern.compile("(\\d{1,3})([WNES])");

    public Piece(int no, Direction direction) {
        this.no = no;
        this.direction = direction;
    }

    public int getNo() {
        return this.no;
    }
    public Direction getDirection() { return this.direction; }

    public static Piece parsePiece(String segment) {
        Piece result;
        Matcher matcher = segmentPattern.matcher(segment);
        if(matcher.find()) {
            result = new Piece( Integer.parseInt(matcher.group(1)), Direction.parseDirection(matcher.group(2)) );
        } else {
            throw new IllegalArgumentException(segment+" does not match the Piece pattern");
        }
        return result;
    }

    @Override
    public String toString() {
        return Integer.toString(this.no) + this.direction.toLetter();
    }
}
