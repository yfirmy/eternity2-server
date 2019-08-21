package fr.firmy.lab.eternity2server.model;

public enum Direction {
    WEST,
    NORTH,
    EAST,
    SOUTH;
    Direction() {
    }

    public static Direction parseDirection(String direction) {
        Direction result;
        switch(direction) {
            case "W":
            case "WEST" : result = Direction.WEST; break;
            case "N":
            case "NORTH" : result = Direction.NORTH; break;
            case "E":
            case "EAST" : result = Direction.EAST; break;
            case "S":
            case "SOUTH" : result = Direction.SOUTH; break;
            default:
                throw new IllegalArgumentException(direction +" is an unknown Direction");
        }
        return result;
    }

    public char toLetter() {
        return this.name().charAt(0);
    }
}
