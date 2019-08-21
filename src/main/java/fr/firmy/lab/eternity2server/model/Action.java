package fr.firmy.lab.eternity2server.model;

public enum Action {
    DONE,
    GO,
    PENDING;

    public static Action parseAction(String action) {
        Action result;
        switch(action) {
            case "DONE":
                result = DONE;
                break;
            case "GO":
                result = GO;
                break;
            case "PENDING":
                result = PENDING;
                break;
            default:
                throw new IllegalArgumentException(action + " is an unknown Action");
        }
        return result;
    }
}
