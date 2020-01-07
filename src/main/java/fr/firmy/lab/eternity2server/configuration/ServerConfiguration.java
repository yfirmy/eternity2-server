package fr.firmy.lab.eternity2server.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {

    DataSource dataSource;

    @Value("${solver.subjobs.url}")
    String subJobsRequest;

    @Value("${solver.subjobs.health.url}")
    String subJobsHealthRequest;

    @Value("${influxdb.url}")
    String influxDbUrl;

    @Value("${influxdb.db-name}")
    String influxDbName;

    @Value("${influxdb.retention-policy}")
    String influxDbRetentionPolicy;

    @Value("${board.size}")
    Integer boardSize;

    @Autowired
    public ServerConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    DataSource getDataSource() {
        return this.dataSource;
    }

    public String getSubJobsRequest() {
        return subJobsRequest;
    }

    public void setSubJobsRequest(String subJobsRequest) {
        this.subJobsRequest = subJobsRequest;
    }

    public String getSubJobsHealthRequest() {
        return this.subJobsHealthRequest;
    }

    public void setSubJobsHealthRequest(String subJobsHealthRequest) {
        this.subJobsHealthRequest = subJobsHealthRequest;
    }

    public String getInfluxDbUrl() {
        return influxDbUrl;
    }

    public void setInfluxDbUrl(String influxDbUrl) {
        this.influxDbUrl = influxDbUrl;
    }

    public String getInfluxDbName() {
        return influxDbName;
    }

    public void setInfluxDbName(String influxDbName) {
        this.influxDbName = influxDbName;
    }

    public String getInfluxDbRetentionPolicy() {
        return influxDbRetentionPolicy;
    }

    public void setInfluxDbRetentionPolicy(String influxDbRetentionPolicy) {
        this.influxDbRetentionPolicy = influxDbRetentionPolicy;
    }

    public Integer getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(Integer boardSize) {
        this.boardSize = boardSize;
    }

}
