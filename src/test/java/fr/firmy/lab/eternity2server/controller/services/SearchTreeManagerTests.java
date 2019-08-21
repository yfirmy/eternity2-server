package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.controller.exception.*;

import fr.firmy.lab.eternity2server.model.Node;
import fr.firmy.lab.eternity2server.utils.TestDataLoader;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static fr.firmy.lab.eternity2server.model.Action.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchTreeManagerTests {

    @Autowired
    SearchTreeManager searchTreeManager;

    @Autowired
    TestDataLoader testDataLoader;

    @Before
    public void setUp() {
        testDataLoader.loadData();
    }

    @After
    public void cleanUp() {
        testDataLoader.deleteData();
    }

    @Test
    public void test_isParentOf() throws MalformedMaterializedPathException {

        MaterializedPath parent = new MaterializedPath("215N.203S");
        MaterializedPath child = new MaterializedPath("215N.203S.600E");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "direct parent" ).isTrue();

        parent = new MaterializedPath("");
        child = new MaterializedPath("215N");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "direct parent from root" ).isTrue();

        parent = new MaterializedPath("");
        child = new MaterializedPath("215N.203S.600E");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "ancestor is not a first level parent" ).isFalse();

        parent = new MaterializedPath("215N.203S.500E");
        child = new MaterializedPath("215N.203S.600E");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "not parent (but siblings)" ).isFalse();

        parent = new MaterializedPath("215N.203S.600E");
        child = new MaterializedPath("215N.203S.600E");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "not its own parent" ).isFalse();

        parent = new MaterializedPath("215N.203S.600E");
        child = new MaterializedPath("215N.203S");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "the child is not parent of his parent" ).isFalse();

        parent = new MaterializedPath("215N.203S.600E");
        child = new MaterializedPath("");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "the child is not parent of an ancestor" ).isFalse();

        parent = new MaterializedPath("215N");
        child = new MaterializedPath("");
        assertThat( searchTreeManager.isParentOf(parent, child) ).as( "the child is not parent of an ancestor (2)" ).isFalse();

    }

    @Test
    public void test_getDonePaths() {

        for( int i=10; i>=4; i--) {
            List<MaterializedPath> paths = searchTreeManager.getDonePaths(i);
            assertThat(paths.size()).as("Done paths of level "+i).isEqualTo(0);
        }

        List<MaterializedPath> pathsLevel3 = searchTreeManager.getDonePaths(3);
        assertThat(pathsLevel3.size()).as("Done paths count of level 7").isEqualTo(1);
        assertThat(pathsLevel3.get(0).toString()).as("Done path of level 3").isEqualTo("215N.200N.700E");

        List<MaterializedPath> pathsLevel2 = searchTreeManager.getDonePaths(2);
        assertThat(pathsLevel2.size()).as("Done paths count of level 8").isEqualTo(1);
        assertThat(pathsLevel2.get(0).toString()).as("Done path of level 2").isEqualTo("213S.202W");

        List<MaterializedPath> pathsLevel1 = searchTreeManager.getDonePaths(1);
        assertThat(pathsLevel1.size()).as("Done paths count of level 9").isEqualTo(1);
        assertThat(pathsLevel1.get(0).toString()).as("Done path of level 1").isEqualTo("200W");
    }

    @Test
    public void test_getPendingPaths() {

        for( int i=10; i>=4; i--) {
            List<MaterializedPath> paths = searchTreeManager.getPendingPaths(i);
            assertThat(paths.size()).as("Pending paths count of level "+i).isEqualTo(0);
        }

        List<MaterializedPath> pathsLevel3 = searchTreeManager.getPendingPaths(3);
        assertThat(pathsLevel3.size()).as("Pending paths count of level 3").isEqualTo(1);
        assertThat(pathsLevel3.get(0).toString()).as("Pending path of level 3").isEqualTo("215N.200N.800E");

        for( int i=2; i>=1; i--) {
            List<MaterializedPath> paths = searchTreeManager.getPendingPaths(i);
            assertThat(paths.size()).as("Pending paths count of level "+i).isEqualTo(0);
        }
    }

    @Test
    public void test_getPathsToDo() {

        for( int i=10; i>=4; i--) {
            List<MaterializedPath> doneJobs = searchTreeManager.getPathsToDo(i, null, null);
            assertThat(doneJobs.size()).as("Done paths count of level "+i).isEqualTo(0);
        }

        List<String> pathsLevel2 = searchTreeManager.getPathsToDo(2, null, null)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2.size()).as("Paths to do count of level 2").isEqualTo(2);
        assertThat(pathsLevel2).as("Paths to do of level 2").contains("213S.201N");
        assertThat(pathsLevel2).as("Paths to do of level 2").contains("215N.203S");

        List<String> pathsLevel1 = searchTreeManager.getPathsToDo(1, null, null)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsLevel1.size()).as("Paths to do count of level 1").isEqualTo(1);
        assertThat(pathsLevel1).as("Paths to do of level 1").contains("212W");
    }

    @Test
    public void test_addPath() throws MalformedMaterializedPathException, MaterializedPathAddFailedException {

        searchTreeManager.addPath( new Node( new MaterializedPath("215N.203S.600E"), GO ) );
        searchTreeManager.addPath( new Node( new MaterializedPath("215N.203S.900E"), GO ) );

        List<String> pathsLevel3 = searchTreeManager.getPathsToDo(3, null, null)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsLevel3.size()).as("Paths to do count of level 3").isEqualTo(2);
        assertThat(pathsLevel3).as("Paths to do of level 3").contains("215N.203S.600E");
        assertThat(pathsLevel3).as("Paths to do of level 3").contains("215N.203S.900E");
    }

    @Test
    public void test_removePath() throws MalformedMaterializedPathException, MaterializedPathRemoveFailedException {

        searchTreeManager.removePath( new Node( new MaterializedPath("213S.202W"), DONE ));

        List<String> pathsLevel2 = searchTreeManager.getDonePaths(2).stream()
                .map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsLevel2.size()).as("Paths to do count of level 2").isEqualTo(0);
    }


    @Test(expected = MaterializedPathAddFailedException.class)
    public void test_addPath_duplicate() throws MalformedMaterializedPathException, MaterializedPathAddFailedException {
        searchTreeManager.addPath( new Node( new MaterializedPath("215N.203S.600E"), GO) );
        searchTreeManager.addPath( new Node( new MaterializedPath("215N.203S.600E"), GO) ); // replicated addition on purpose
    }

    @Test(expected = MaterializedPathRemoveFailedException.class)
    public void test_removePath_not_exists() throws MalformedMaterializedPathException, MaterializedPathRemoveFailedException {
        searchTreeManager.removePath( new Node( new MaterializedPath("212N"), DONE ));
    }

    @Test
    public void test_getChildren_from_root() throws MalformedMaterializedPathException {
        List<Node> children = searchTreeManager.getChildren(new MaterializedPath(""));
        List<String> childrenPaths = children.stream().map(node -> node.toString()).collect(Collectors.toList());
        assertThat(childrenPaths).as("Children of root").contains("200W.DONE");
        assertThat(childrenPaths).as("Children of root").contains("212W.GO");
        assertThat(childrenPaths).as("Children of root").contains("213S.202W.DONE");
        assertThat(childrenPaths).as("Children of root").contains("215N.200N.700E.DONE");
        assertThat(childrenPaths).as("Children of root").contains("215N.200N.800E.PENDING");
        assertThat(childrenPaths).as("Children of root").contains("215N.203S.GO");
        assertThat(childrenPaths.size()).as("Children count of root").isEqualTo(7);
    }

    @Test
    public void test_getChildren_from_branch() throws MalformedMaterializedPathException {
        List<Node> children = searchTreeManager.getChildren(new MaterializedPath("213S"));
        List<String> childrenPaths = children.stream().map(node -> node.toString()).collect(Collectors.toList());
        //assertThat(childrenPaths).as("Children of a root").contains("213S.201N");
        //assertThat(childrenPaths).as("Children of a root").contains("213S.202W");
        assertThat(childrenPaths).as("Children of a branch").contains("213S.201N.GO");
        assertThat(childrenPaths).as("Children of a branch").contains("213S.202W.DONE");
        assertThat(childrenPaths.size()).as("Children count of a branch").isEqualTo(2);
    }

    @Test
    public void test_getChildren_from_leaf() throws MalformedMaterializedPathException {
        List<Node> children = searchTreeManager.getChildren(new MaterializedPath("215N.203S"));
        List<String> childrenPaths = children.stream().map(node -> node.toString()).collect(Collectors.toList());
        assertThat(childrenPaths.size()).as("Children count of a leaf").isEqualTo(0);
    }

    @Test
    public void test_getParent_from_root() throws MalformedMaterializedPathException {
        Optional<MaterializedPath> parent = searchTreeManager.getParent(new MaterializedPath(""));
        assertThat(parent.isPresent()).as("A root's parent is absent").isFalse();
    }

    @Test
    public void test_getParent_from_leaf() throws MalformedMaterializedPathException {
        Optional<MaterializedPath> parent = searchTreeManager.getParent(new MaterializedPath("215N.203S"));
        assertThat(parent.isPresent()).as("A leaf's parent is present").isTrue();
        assertThat(parent.get().toString()).as("Leaf's parent").isEqualTo("215N");
        Optional<MaterializedPath> grandparent = searchTreeManager.getParent(parent.get());
        assertThat(grandparent.isPresent()).as("A leaf's grand-parent is present").isTrue();
        assertThat(grandparent.get().toString()).as("Leaf's grand-parent").isEqualTo("");
    }

    @Test
    public void test_replacePath_nominal() throws MalformedMaterializedPathException, MaterializedPathReplaceFailedException {
        Node toRemove = new Node( new MaterializedPath("215N.203S"), GO);
        Node toAdd = new Node( new MaterializedPath("215N.203S"), DONE);

        List<String> pathsToDo_before = searchTreeManager.getPathsToDo(2, null, null)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsToDo_before.size()).as("Paths to do count of level 2 before replace").isEqualTo(2);
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").contains("213S.201N");
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").contains("215N.203S");

        List<String> pathsDone_before = searchTreeManager.getDonePaths(2)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsDone_before.size()).as("Paths Done count of level 2 before replace").isEqualTo(1);
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").contains("213S.202W");
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").doesNotContain("215N.203S");

        searchTreeManager.replacePath(Arrays.asList(toRemove), Arrays.asList(toAdd));

        List<String> pathsToDo_after = searchTreeManager.getPathsToDo(2, null, null)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsToDo_after.size()).as("Paths to do count of level 2 after replace").isEqualTo(1);
        assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").contains("213S.201N");
        assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").doesNotContain("215N.203S");

        List<String> pathsDone_after = searchTreeManager.getDonePaths(2)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsDone_after.size()).as("Paths Done count of level 2 after replace").isEqualTo(2);
        assertThat(pathsDone_after).as("Paths Done of level 2 after replace").contains("213S.202W");
        assertThat(pathsDone_after).as("Paths Done of level 2 after replace").contains("215N.203S");

    }

    @Test(expected = MaterializedPathReplaceFailedException.class)
    public void test_replacePath_error() throws MalformedMaterializedPathException, MaterializedPathReplaceFailedException {
        Node toRemove = new Node(new MaterializedPath("215N.266S"), GO);
        Node toAdd = new Node(new MaterializedPath("215N.203S"), DONE);

        List<String> pathsToDo_before = searchTreeManager.getPathsToDo(2, null, null)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsToDo_before.size()).as("Paths to do count of level 2 before replace").isEqualTo(2);
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").contains("213S.201N");
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").contains("215N.203S");
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").doesNotContain("215N.266S");

        List<String> pathsDone_before = searchTreeManager.getDonePaths(2)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsDone_before.size()).as("Paths Done count of level 2 before replace").isEqualTo(1);
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").contains("213S.202W");
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").doesNotContain("215N.203S");
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").doesNotContain("215N.266S");

        try {
            searchTreeManager.replacePath(Arrays.asList(toRemove), Arrays.asList(toAdd));
        } catch( Exception e ) {

            List<String> pathsToDo_after = searchTreeManager.getPathsToDo(2, null, null)
                    .stream().map(path -> path.toString()).collect(Collectors.toList());

            assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").contains("213S.201N");
            assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").contains("215N.203S");
            assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").doesNotContain("215N.266S");
            assertThat(pathsToDo_after.size()).as("Paths to do count of level 2 after replace").isEqualTo(2);

            List<String> pathsDone_after = searchTreeManager.getDonePaths(2)
                    .stream().map(path -> path.toString()).collect(Collectors.toList());

            assertThat(pathsDone_after).as("Paths Done of level 2 after replace").contains("213S.202W");
            assertThat(pathsDone_after).as("Paths Done of level 2 after replace").doesNotContain("215N.203S");
            assertThat(pathsDone_after).as("Paths Done of level 2 after replace").doesNotContain("215N.266S");
            assertThat(pathsDone_after.size()).as("Paths Done count of level 2 after replace").isEqualTo(1);
            throw e;
        }
    }

    @Test(expected = MaterializedPathReplaceFailedException.class)
    public void test_replacePath_identical() throws MalformedMaterializedPathException, MaterializedPathReplaceFailedException {
        Node toRemove = new Node( new MaterializedPath("215N.203S"), GO);
        Node toAdd = new Node( new MaterializedPath("215N.203S"), GO);

        List<String> pathsToDo_before = searchTreeManager.getPathsToDo(2, null, null)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsToDo_before.size()).as("Paths to do count of level 2 before replace").isEqualTo(2);
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").contains("213S.201N");
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").contains("215N.203S");
        assertThat(pathsToDo_before).as("Paths to do of level 2 before replace").doesNotContain("215N.266S");

        List<String> pathsDone_before = searchTreeManager.getDonePaths(2)
                .stream().map( path -> path.toString() ).collect(Collectors.toList());
        assertThat(pathsDone_before.size()).as("Paths Done count of level 2 before replace").isEqualTo(1);
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").contains("213S.202W");
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").doesNotContain("215N.203S");
        assertThat(pathsDone_before).as("Paths Done of level 2 before replace").doesNotContain("215N.266S");

        try {
            searchTreeManager.replacePath(Arrays.asList(toRemove), Arrays.asList(toAdd));
        } catch( Exception e ) {

            List<String> pathsToDo_after = searchTreeManager.getPathsToDo(2, null, null)
                    .stream().map(path -> path.toString()).collect(Collectors.toList());
            assertThat(pathsToDo_after.size()).as("Paths to do count of level 2 after replace").isEqualTo(2);
            assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").contains("213S.201N");
            assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").contains("215N.203S");
            assertThat(pathsToDo_after).as("Paths to do of level 2 after replace").doesNotContain("215N.266S");

            List<String> pathsDone_after = searchTreeManager.getDonePaths(2)
                    .stream().map(path -> path.toString()).collect(Collectors.toList());
            assertThat(pathsDone_after.size()).as("Paths Done count of level 2 after replace").isEqualTo(1);
            assertThat(pathsDone_after).as("Paths Done of level 2 after replace").contains("213S.202W");
            assertThat(pathsDone_after).as("Paths Done of level 2 after replace").doesNotContain("215N.203S");
            assertThat(pathsDone_after).as("Paths Done of level 2 after replace").doesNotContain("215N.266S");

            throw e;
        }
    }

}
