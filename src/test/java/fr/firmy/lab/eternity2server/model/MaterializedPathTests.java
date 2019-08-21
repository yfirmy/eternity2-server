package fr.firmy.lab.eternity2server.model;

import fr.firmy.lab.eternity2server.controller.exception.MalformedMaterializedPathException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MaterializedPathTests {

    @Test
    public void test_constructor() throws MalformedMaterializedPathException {

        MaterializedPath path1 = new MaterializedPath("215N.203S");
        List<String> segments = Arrays.asList("215N", "203S");
        assertThat(path1.toString()).as("built from string representation").isEqualTo("215N.203S");
    }
}
