package fr.firmy.lab.eternity2server.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ServerConfiguration {

    @Autowired
    DataSource dataSource;

    @Value("${solver.subjob.url}")
    String subJobsRequest;

    @Value("${board.size}")
    Integer boardSize;

    DataSource getDataSource() {
        return this.dataSource;
    }

    public String getSubJobsRequest() {
        return subJobsRequest;
    }

    public void setSubJobsRequest(String subJobsRequest) {
        this.subJobsRequest = subJobsRequest;
    }

    public Integer getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(Integer boardSize) {
        this.boardSize = boardSize;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
