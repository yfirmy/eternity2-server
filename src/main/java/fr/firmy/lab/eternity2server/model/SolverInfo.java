package fr.firmy.lab.eternity2server.model;

import java.net.InetAddress;

public class SolverInfo {

    private String name;
    private InetAddress ip;
    private String version;
    private String machineType;
    private String clusterName;

    private Double score;

    public SolverInfo() {
    }

    public SolverInfo(String name, InetAddress ip, String version, String machineType, String clusterName, Double score) {
        this.name = name;
        this.ip = ip;
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

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
