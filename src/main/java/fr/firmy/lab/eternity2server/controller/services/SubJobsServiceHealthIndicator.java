package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SubJobsServiceHealthIndicator extends AbstractHealthIndicator {

    private static Logger LOGGER = LoggerFactory.getLogger( SubJobsServiceHealthIndicator.class.getName() );
    private final RestTemplate restTemplate;
    private final String subJobsHealthRequest;

    @Autowired
    public SubJobsServiceHealthIndicator(RestTemplate restTemplate, ServerConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.subJobsHealthRequest = configuration.getSubJobsHealthRequest() ;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        try {
            restTemplate.exchange(
                    subJobsHealthRequest,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Void>() {
                    });

            builder.up().build();

        } catch( Exception  e ) {
            LOGGER.error("Sub-Jobs Service is not available");
            builder.down().build();
        }
    }
}
