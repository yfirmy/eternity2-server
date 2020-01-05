package fr.firmy.lab.eternity2server.controller.dal;

import fr.firmy.lab.eternity2server.controller.exception.MaterializedPathAddFailedException;
import fr.firmy.lab.eternity2server.controller.exception.MaterializedPathRemoveFailedException;
import fr.firmy.lab.eternity2server.controller.exception.MaterializedPathReplaceFailedException;
import fr.firmy.lab.eternity2server.controller.exception.MaterializedPathUpdateFailedException;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Node;

import java.util.List;
import java.util.Optional;

public interface SearchTreeManager {

    List<MaterializedPath> getPathsAtLevel(Action tag, int level, Integer limit, Integer offset);

    void addPath(Node node) throws MaterializedPathAddFailedException;
    void removePath(Node node) throws MaterializedPathRemoveFailedException;
    void updateTag(Node node, Action newTag) throws MaterializedPathUpdateFailedException;

    List<Node> getChildren(MaterializedPath materializedPath);
    Optional<MaterializedPath> getParent(MaterializedPath materializedPath);
    boolean isParentOf(MaterializedPath parent, MaterializedPath child);
    boolean isAncestorOf(MaterializedPath parent, MaterializedPath child);

    void replacePath(List<Node> listToRemove, List<Node> listToAdd) throws MaterializedPathReplaceFailedException;

    List<Node> getAllPaths();

    List<MaterializedPath> getDonePaths(int level);
    List<MaterializedPath> getPendingPaths(int level);
    List<MaterializedPath> getPathsToDo(int level, Integer limit, Integer offset);
}
