package fr.firmy.lab.eternity2server;

import fr.firmy.lab.eternity2server.controller.*;
import fr.firmy.lab.eternity2server.controller.services.JobsServiceTests;
import fr.firmy.lab.eternity2server.controller.dal.SolutionsRepositoryTests;
import fr.firmy.lab.eternity2server.controller.dal.SearchTreeManagerTests;
import fr.firmy.lab.eternity2server.model.JobTests;
import fr.firmy.lab.eternity2server.model.MaterializedPathTests;
import fr.firmy.lab.eternity2server.model.adapter.MaterializedPathAdapterTests_Size25;
import fr.firmy.lab.eternity2server.model.adapter.MaterializedPathAdapterTests_Size36;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        MaterializedPathTests.class,
        MaterializedPathAdapterTests_Size25.class,
        MaterializedPathAdapterTests_Size36.class,
        JobTests.class,
        JobsServiceTests.class,
        SearchTreeManagerTests.class,
        SolutionsRepositoryTests.class,
        HttpRestControllerTests.class
})
public class Eternity2ServerUnitTests {
}
