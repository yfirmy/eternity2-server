package fr.firmy.lab.eternity2server.model.dto;

public class BoardDescription {

    private String representation;

    private static final String separator = ":";

    public BoardDescription() {}

    public BoardDescription(String representation) {
        this.representation = representation;
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

}
