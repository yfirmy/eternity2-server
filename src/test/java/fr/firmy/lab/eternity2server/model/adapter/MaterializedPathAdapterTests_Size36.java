package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.exception.MalformedBoardDescriptionException;
import fr.firmy.lab.eternity2server.controller.exception.MalformedMaterializedPathException;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.dto.BoardDescription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"board.size=36"})
public class MaterializedPathAdapterTests_Size36 {

    @Autowired
    MaterializedPathAdapter adapter;

    @Autowired
    ServerConfiguration config;

    @Test
    public void test_fromBoardDescription_1() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$13N:21N:17N:15E:24N:25N:12E:28N:29N:34S:31S:27N:9W:3N:1S:0S:26S:16W:10W:4S:5E:2W:18S:19N:14S:32E:8N:7N:23N:11N:33S:20W:30N:22W:6W:35N:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("Example from Clue 1")
                .isEqualTo("13N.25N.33S.35N.21N.17N.15E.24N.27N.16W.19N.11N.6W.22W.30N.20W.14S.10W.9W.12E.28N.29N.34S.31S.26S.18S.23N.7N.8N.32E.4S.3N.1S.0S.2W.5E");
    }

    @Test
    public void test_toBoardDescription_1() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("13N.25N.33S.35N.21N.17N.15E.24N.27N.16W.19N.11N.6W.22W.30N.20W.14S.10W.9W.12E.28N.29N.34S.31S.26S.18S.23N.7N.8N.32E.4S.3N.1S.0S.2W.5E");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("Example from Clue 1")
                .isEqualTo("$13N:21N:17N:15E:24N:25N:12E:28N:29N:34S:31S:27N:9W:3N:1S:0S:26S:16W:10W:4S:5E:2W:18S:19N:14S:32E:8N:7N:23N:11N:33S:20W:30N:22W:6W:35N:;");

    }

}
