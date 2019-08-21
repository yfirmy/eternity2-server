package fr.firmy.lab.eternity2server.model;

public class Job {

    private Board board;
    private Action action;

    public Job(Board board, Action action) {
        this.board = board;
        this.action = action;
    }

    public Job(Job other, Action action) {
        this(other.board, action);
    }

    public boolean isDone() {
        return this.action.equals(Action.DONE);
    }

    public boolean isPending() {
        return this.action.equals(Action.PENDING);
    }

    public int getSize() {
        return this.board.getSize() - this.board.getPiecesCount();
    }

    public Board getBoard() {
        return this.board;
    }

    public Action getAction() {
        return this.action;
    }
}
