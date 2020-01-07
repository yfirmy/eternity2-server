package fr.firmy.lab.eternity2server.configuration;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableScheduling
public class ApacheHttpClientConfiguration {

    // Connection pool
    final int MAX_TOTAL_CONNECTIONS = 20;
    final int MAX_ROUTE_CONNECTIONS = 20;

    // Keep alive
    int DEFAULT_KEEP_ALIVE_TIME = 20 * 1000; // 20 sec

    // Timeouts
    int CONNECTION_TIMEOUT = 5 * 1000; // 30 sec, the time for waiting until a connection is established
    int REQUEST_TIMEOUT    = 5 * 1000; // 30 sec, the time for waiting for a connection from connection pool
    int SOCKET_TIMEOUT     = 5 * 1000; // 60 sec, the time for waiting for data

    // Idle connection monitor
    int IDLE_CONNECTION_WAIT_TIME = 30 * 1000; // 30 sec

    @Value("${solver.subjobs.pool.maxConnections}") // 5
    Integer maxConnections;

    private ServerConfiguration serverConfiguration;

    @Autowired
    public ApacheHttpClientConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();

        // set a total amount of connections across all HTTP routes
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        // set a maximum amount of connections for each HTTP route in pool
        poolingConnectionManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString( serverConfiguration.getSubJobsRequest() );
        UriComponents subJobsUri = uriComponentsBuilder.build();
        HttpHost subJobsHost = new HttpHost( subJobsUri.getHost(), subJobsUri.getPort() );
        poolingConnectionManager.setMaxPerRoute(new HttpRoute(subJobsHost), maxConnections);

        return poolingConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (httpResponse, httpContext) -> {
            HeaderIterator headerIterator = httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE);
            HeaderElementIterator elementIterator = new BasicHeaderElementIterator(headerIterator);

            while (elementIterator.hasNext()) {
                HeaderElement element = elementIterator.nextElement();
                String param = element.getName();
                String value = element.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000; // convert to ms
                }
            }

            return DEFAULT_KEEP_ALIVE_TIME;
        };
    }

    @Bean
    public Runnable idleConnectionMonitor(PoolingHttpClientConnectionManager pool) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 20000)
            public void run() {
                // only if connection pool is initialised
                if (pool != null) {
                    pool.closeExpiredConnections();
                    pool.closeIdleConnections( IDLE_CONNECTION_WAIT_TIME, TimeUnit.MILLISECONDS);
                }
            }
        };
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("idleMonitor");
        scheduler.setPoolSize(5);
        return scheduler;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager())
                .setKeepAliveStrategy(connectionKeepAliveStrategy())
                .build();
    }
}
