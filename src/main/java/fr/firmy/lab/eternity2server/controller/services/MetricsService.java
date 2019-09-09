package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private static final String selectDepthRequest  =
            "SELECT nlevel(path) AS depth FROM search.tree ORDER BY nlevel(path) DESC LIMIT 1";

    private static final String selectPendingJobsCountRequest =
            "SELECT count(path) FROM search.tree WHERE tag='PENDING';";

    private static final String selectDoneJobsCountRequest =
            "SELECT count(path) FROM search.tree WHERE tag='DONE';";

    private static final String selectTodoJobsCountRequest =
            "SELECT count(path) FROM search.tree WHERE tag='GO';";

    private static final String selectTotalLeavesCountRequest =
            "SELECT count(path) AS total FROM search.tree";

    private static final String selectResultsCountRequest =
            "SELECT count(path) AS total FROM search.results";

    private static final String selectWidthAtLevelRequest =
            "SELECT count(path) AS width FROM search.tree WHERE nlevel(path)= ?";

    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbcTemplate;
    private final int boardSize;

    @Autowired
    public MetricsService(JdbcTemplate jdbcTemplate, MeterRegistry meterRegistry, ServerConfiguration configuration) {
        this.meterRegistry = meterRegistry;
        this.jdbcTemplate = jdbcTemplate;
        this.boardSize = configuration.getBoardSize();

        gauge(search_tree("depth"), "Search Tree Depth", selectDepthRequest);
        histogram(search_tree("width"), "Search Tree Width", selectWidthAtLevelRequest);

        gauge(search_tree("jobs.pending.count"), "Pending Jobs Count", selectPendingJobsCountRequest);
        gauge(search_tree("jobs.done.count"), "Done Jobs Count", selectDoneJobsCountRequest);
        gauge(search_tree("jobs.todo.count"), "Jobs To Do Count", selectTodoJobsCountRequest);
        gauge(search_tree("leaves.count"), "Total Leaves Count", selectTotalLeavesCountRequest);
        gauge(search_results("count"), "Search Results Count", selectResultsCountRequest);
    }

    private String search_tree(String metricName) {
        return name("application", "search_tree", metricName);
    }

    private String search_results(String metricName) {
        return name("application", "search_results", metricName);
    }

    private String name(String prefix, String name, String suffix) {
        return prefix+"."+name+"."+suffix;
    }

    private void gauge(String name, String description, String sqlQuery) {
        Gauge.builder(name,
                ()-> jdbcTemplate.query(sqlQuery, rs -> { rs.next(); return rs.getInt(1); })  )
                .description(description)
                .register(meterRegistry);
    }

    private void histogram(String name, String description, String sqlQuery) {
        for( int depth = 1; depth <= boardSize; depth++ ) {
            final int level = depth;
            Gauge.builder(name,
                    () -> jdbcTemplate.query(sqlQuery, rs -> { rs.next(); return rs.getInt(1); }, level))
                    .description(description)
                    .tag("depth", Integer.toString(depth))
                    .register(meterRegistry);
        }
    }

}
