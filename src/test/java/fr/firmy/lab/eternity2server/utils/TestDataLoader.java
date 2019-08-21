package fr.firmy.lab.eternity2server.utils;

import fr.firmy.lab.eternity2server.model.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static fr.firmy.lab.eternity2server.model.Action.*;

@Component
public class TestDataLoader {

    private static String insertRequest =
            "INSERT INTO search.tree (creation_time, path, tag) VALUES (DEFAULT, ?::ltree, ?::search.action)";

    private static String truncateRequest =
            "TRUNCATE search.tree";

    private static String insertResult =
            "INSERT INTO search.results (creation_time, path) VALUES (DEFAULT, ?::ltree)";

    private static String truncateResults =
            "TRUNCATE search.results";

    @Autowired
    private JdbcTemplate eternity2JdbcTemplate;

    @Autowired
    DataSource dataSource;

    public void insertPath(String path, Action tag) {
        eternity2JdbcTemplate.update(insertRequest, path, tag.name());
    }

    public void loadData() {
        insertPath("212W", GO);
        insertPath("215N.200N.700E", DONE);
        insertPath("215N.200N.800E", PENDING);
        insertPath("215N.203S", GO);
        insertPath("213S.201N", GO);
        insertPath("213S.202W", DONE);
        insertPath("200W", DONE);

        insertResult("1N.2W.3S.4E.5N.6W.7S.8E.9N");
    }

    public void loadEmptyTree() {
        insertPath("", GO);
    }

    public void deleteData() {
        eternity2JdbcTemplate.update(truncateRequest);
        eternity2JdbcTemplate.update(truncateResults);
    }

    private void insertResult(String path) {
        eternity2JdbcTemplate.update(insertResult, path);
    }

}
