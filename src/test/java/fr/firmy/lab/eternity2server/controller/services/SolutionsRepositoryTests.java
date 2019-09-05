package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Solution;
import fr.firmy.lab.eternity2server.utils.TestDataHelper;
import fr.firmy.lab.eternity2server.utils.TestDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import fr.firmy.lab.eternity2server.controller.exception.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SolutionsRepositoryTests {

    @Autowired
    SolutionsRepository solutionsRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TestDataLoader testDataLoader;

    @Autowired
    TestDataHelper testDataHelper;

    @Before
    public void setUp() {
        testDataLoader.loadData();
    }

    @After
    public void cleanUp() {
        testDataLoader.deleteData();
    }

    private void addSolution(String boardDescription) throws ResultSubmissionFailedException {
        solutionsRepository.addSolutions(Collections.singletonList(testDataHelper.buildSolution(boardDescription)));
    }

    @Test
    public void test_addResults_nominal() throws ResultSubmissionFailedException {

        addSolution("$9W:5W:8S:2N:1W:4S:7E:3E:6N:;");

        List<String> solutions = solutionsRepository.getSolutions(null, null)
                .stream()
                .map(Solution::getPath)
                .map( MaterializedPath::toString ).collect(Collectors.toList());

        assertThat(solutions).as("Solutions found").contains("1N.2W.3S.4E.5N.6W.7S.8E.9N");
        assertThat(solutions).as("Solutions found").contains("9W.8S.7E.6N.5W.4S.3E.2N.1W");
        assertThat(solutions.size()).as("Solutions").isEqualTo(2);
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_addResults_error_rewrite_1() throws ResultSubmissionFailedException {

        try {
            addSolution("$1N:5N:2W:8E:9N:6W:3S:7S:4E:;");
        }
        catch(Exception e) {

            List<String> solutions = solutionsRepository.getSolutions(null, null)
                    .stream()
                    .map(Solution::getPath)
                    .map(MaterializedPath::toString)
                    .collect(Collectors.toList());

            assertThat(solutions).as("Solutions found").contains("1N.2W.3S.4E.5N.6W.7S.8E.9N");
            assertThat(solutions.size()).as("Solutions").isEqualTo(1);

            throw e;
        }
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_addResults_error_rewrite_2() throws ResultSubmissionFailedException {

        addSolution("$9W:5W:8S:2N:1W:4S:7E:3E:6N:;");

        try {
            addSolution("$9W:5W:8S:2N:1W:4S:7E:3E:6N:;");
        }
        catch(Exception e) {

            List<String> solutions = solutionsRepository.getSolutions(null, null)
                    .stream()
                    .map(Solution::getPath)
                    .map(MaterializedPath::toString).collect(Collectors.toList());

            assertThat(solutions).as("Solutions found").contains("1N.2W.3S.4E.5N.6W.7S.8E.9N");
            assertThat(solutions).as("Solutions found").contains("9W.8S.7E.6N.5W.4S.3E.2N.1W");
            assertThat(solutions.size()).as("Solutions").isEqualTo(2);

            throw e;
        }
    }

    @Test( expected = ResultSubmissionFailedException.class )
    public void test_addResults_error_wrong_termination() throws ResultSubmissionFailedException {

        try {
            addSolution("$9W:8S:7E:6N:5W:4S:3E:2N:.:;");
        }
        catch(Exception e) {

            List<String> solutions = solutionsRepository.getSolutions(null, null)
                    .stream()
                    .map(Solution::getPath)
                    .map(MaterializedPath::toString).collect(Collectors.toList());

            assertThat(solutions).as("Solutions found").contains("1N.2W.3S.4E.5N.6W.7S.8E.9N");
            assertThat(solutions.size()).as("Solutions").isEqualTo(1);

            throw e;
        }
    }

}
