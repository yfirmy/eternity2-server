package fr.firmy.lab.eternity2server.model.dto;

public class SolverDescription {

    private String name;
    private String version;
    private String machineType;
    private String clusterName;
    private String score;

    public SolverDescription() {
    }

    public SolverDescription(String name, String version, String machineType, String clusterName, String score) {
        this.name = name;
        this.version = version;
        this.machineType = machineType;
        this.clusterName = clusterName;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
