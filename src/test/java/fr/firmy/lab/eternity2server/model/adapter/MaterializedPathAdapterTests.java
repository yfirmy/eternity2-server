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
@SpringBootTest(properties = {"board.size=25"})
public class MaterializedPathAdapterTests {

    @Autowired
    MaterializedPathAdapter adapter;

    @Autowired
    ServerConfiguration config;

    @Test
    public void test_fromBoardDescription_1() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (empty board)")
                .isEqualTo("");
    }

    @Test
    public void test_fromBoardDescription_2() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (just one piece)")
                .isEqualTo("1W");
    }

    @Test
    public void test_fromBoardDescription_3() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:.:.:.:5N:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:21E:.:.:.:25W:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (just the 4 corner pieces)")
                .isEqualTo("1W.5N.21E.25W");
    }

    @Test
    public void test_fromBoardDescription_4() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:2E:3S:4W:5N:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:21E:.:.:.:25W:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (then the top row)")
                .isEqualTo("1W.5N.21E.25W.2E.3S.4W");
    }

    @Test
    public void test_fromBoardDescription_5() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:2E:3S:4W:5N:.:.:.:.:10W:.:.:.:.:15E:.:.:.:.:20S:21E:.:.:.:25W:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (then the right column)")
                .isEqualTo("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S");
    }

    @Test
    public void test_fromBoardDescription_6() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:2E:3S:4W:5N:.:.:.:.:10W:.:.:.:.:15E:.:.:.:.:20S:21E:22W:23S:24E:25W:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (then the bottom row)")
                .isEqualTo("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W");
    }

    @Test
    public void test_fromBoardDescription_7() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:2E:3S:4W:5N:6W:.:.:.:10W:11E:.:.:.:15E:16S:.:.:.:20S:21E:22W:23S:24E:25W:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (then the left column)")
                .isEqualTo("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W.16S.11E.6W");
    }

    @Test
    public void test_fromBoardDescription_8() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:2E:3S:4W:5N:6W:7N:8W:9E:10W:11E:12N:.:14S:15E:16S:17S:18E:19W:20S:21E:22W:23S:24E:25W:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (then the inner ring)")
                .isEqualTo("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W.16S.11E.6W.7N.8W.9E.14S.19W.18E.17S.12N");
    }

    @Test
    public void test_fromBoardDescription_9() throws MalformedBoardDescriptionException {

        BoardDescription boardDescription = new BoardDescription("$1W:2E:3S:4W:5N:6W:7N:8W:9E:10W:11E:12N:13W:14S:15E:16S:17S:18E:19W:20S:21E:22W:23S:24E:25W:;");
        MaterializedPath materializedPath = adapter.fromBoardDescription(boardDescription);
        assertThat(materializedPath.toString()).as("the materialized path as a string (then the last piece in the center)")
                .isEqualTo("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W.16S.11E.6W.7N.8W.9E.14S.19W.18E.17S.12N.13W");
    }

    @Test
    public void test_toBoardDescription_1() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the root materialized path")
                .isEqualTo("$.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:;");
    }

    @Test
    public void test_toBoardDescription_2() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:;");
    }

    @Test
    public void test_toBoardDescription_3() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W.5N.21E.25W");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:.:.:.:5N:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:21E:.:.:.:25W:;");
    }

    @Test
    public void test_toBoardDescription_4() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W.5N.21E.25W.2E.3S.4W");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:2E:3S:4W:5N:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:21E:.:.:.:25W:;");
    }

    @Test
    public void test_toBoardDescription_5() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:2E:3S:4W:5N:.:.:.:.:10W:.:.:.:.:15E:.:.:.:.:20S:21E:.:.:.:25W:;");
    }

    @Test
    public void test_toBoardDescription_6() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:2E:3S:4W:5N:.:.:.:.:10W:.:.:.:.:15E:.:.:.:.:20S:21E:22W:23S:24E:25W:;");
    }

    @Test
    public void test_toBoardDescription_7() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W.16S.11E.6W");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:2E:3S:4W:5N:6W:.:.:.:10W:11E:.:.:.:15E:16S:.:.:.:20S:21E:22W:23S:24E:25W:;");
    }

    @Test
    public void test_toBoardDescription_8() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W.16S.11E.6W.7N.8W.9E.14S.19W.18E.17S.12N");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:2E:3S:4W:5N:6W:7N:8W:9E:10W:11E:12N:.:14S:15E:16S:17S:18E:19W:20S:21E:22W:23S:24E:25W:;");
    }

    @Test
    public void test_toBoardDescription_9() throws MalformedMaterializedPathException {

        MaterializedPath materializedPath = new MaterializedPath("1W.5N.21E.25W.2E.3S.4W.10W.15E.20S.24E.23S.22W.16S.11E.6W.7N.8W.9E.14S.19W.18E.17S.12N.13W");
        BoardDescription boardDescription = adapter.toBoardDescription(materializedPath);
        assertThat(boardDescription.getRepresentation()).as("the board representation of the materialized path")
                .isEqualTo("$1W:2E:3S:4W:5N:6W:7N:8W:9E:10W:11E:12N:13W:14S:15E:16S:17S:18E:19W:20S:21E:22W:23S:24E:25W:;");

    }

}
