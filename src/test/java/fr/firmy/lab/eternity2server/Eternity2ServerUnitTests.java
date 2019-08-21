package fr.firmy.lab.eternity2server;

import fr.firmy.lab.eternity2server.controller.*;
import fr.firmy.lab.eternity2server.controller.services.JobsServiceTests;
import fr.firmy.lab.eternity2server.controller.services.SolutionsRepositoryTests;
import fr.firmy.lab.eternity2server.controller.services.SearchTreeManagerTests;
import fr.firmy.lab.eternity2server.model.JobTests;
import fr.firmy.lab.eternity2server.model.MaterializedPathTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        MaterializedPathTests.class,
        JobTests.class,
        JobsServiceTests.class,
        SearchTreeManagerTests.class,
        SolutionsRepositoryTests.class,
        HttpControllerTests.class
})
public class Eternity2ServerUnitTests {
}
