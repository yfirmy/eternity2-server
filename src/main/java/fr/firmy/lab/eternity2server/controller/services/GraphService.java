package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.controller.dal.SearchTreeManager;
import fr.firmy.lab.eternity2server.controller.exception.MalformedMaterializedPathException;
import fr.firmy.lab.eternity2server.model.Action;
import fr.firmy.lab.eternity2server.model.MaterializedPath;
import fr.firmy.lab.eternity2server.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.firmy.lab.eternity2server.model.Action.PENDING;

@Component
public class GraphService {

    private SearchTreeManager searchTreeManager;

    @Autowired
    public GraphService(SearchTreeManager searchTreeManager) {
        this.searchTreeManager = searchTreeManager;
    }

    public String createMermaidDiagram() {

        Tree inMemory = new Tree();
        inMemory.load( searchTreeManager.getAllPaths() );

        StringBuilder output = new StringBuilder();
        append("graph TD\n", output);
        append("classDef classDONE fill:#F08080,stroke:#333,stroke-width:4px;\n", output);
        append("classDef classPENDING fill:#F0E68C,stroke:#333,stroke-width:4px;\n", output);
        append("classDef classGO fill:#90EE90,stroke:#333,stroke-width:4px;\n", output);
        TreeNode rootNode = inMemory.getRoot();
        writeTreeNode( rootNode, output );

        return output.toString();
    }

    private void writeTreeNode(TreeNode node, StringBuilder output) {

        int level = node.materializedPath.segmentsCount();
        String margin = IntStream.range(0, level).mapToObj(i -> " ").collect(Collectors.joining(""));
        List<TreeNode> children = node.getChildren();
        for (TreeNode child : children) {
            if( child.getChildren().isEmpty() ) {
                append(margin + node.id + " --> " + child.id + "(" + child.label + ")\n", output);
                append(margin + "class "+child.id+" class"+child.tag+";\n", output);
            } else {
                append(margin + node.id + " --> " + child.id + "{" + child.label + "}\n", output);
            }
        }
        for (TreeNode child : children) {
            writeTreeNode(child, output);
        }
    }

    private void append(String line, StringBuilder output) {
        System.out.print(line);
        output.append(line);
    }

    private class TreeNode implements Comparable<TreeNode> {
        public String id;
        public String label;
        public MaterializedPath materializedPath;
        public Action tag;
        public TreeNode parent;
        private TreeSet<TreeNode> children;
        public TreeNode(int id, MaterializedPath materializedPath, Action tag) {
            this.id = materializedPath.toString().isEmpty() ? "root" : String.format("%x", id);
            this.materializedPath = materializedPath;
            this.label = materializedPath.toString().isEmpty() ? "root" : materializedPath.getSegments().get( materializedPath.segmentsCount() - 1 );
            this.tag = tag;
            this.children = new TreeSet<>();
        }
        public void setParent(TreeNode parent) {
            this.parent = parent;
        }
        public void addChild(TreeNode child) {
            this.children.add(child);
        }
        public List<TreeNode> getChildren() {
            return new ArrayList<>(this.children);
        }

        @Override
        public int compareTo(TreeNode o) {
            return this.materializedPath.compareTo(o.materializedPath);
        }
    }

    private class Tree {
        private TreeNode root;
        private TreeMap<MaterializedPath, TreeNode> index;
        public Tree() {
            this.index = new TreeMap<>();
        }
        public void load( List<Node> allNodes ) {
            try {
                Node rootNode = new Node( new MaterializedPath(""), PENDING );
                this.root = this.load( rootNode );
                for(Node node: allNodes) {
                    this.load(node);
                }
            } catch(MalformedMaterializedPathException e) {
            }
        }
        private TreeNode load( Node node ) throws MalformedMaterializedPathException {
            MaterializedPath matpath = node.getPath();
            TreeNode treeNode = get(matpath, node.getTag());
            for (int level = 0; level < matpath.getSegments().size(); level++) {
                MaterializedPath parent = new MaterializedPath(String.join(".", matpath.getSegments().subList(0, level)));
                MaterializedPath child = new MaterializedPath(String.join(".", matpath.getSegments().subList(0, level + 1)));
                link(parent, child);
            }
            return treeNode;
        }
        private void link( MaterializedPath parent, MaterializedPath child ) {
            TreeNode parentNode = get(parent, null);
            TreeNode childNode = get(child, null);
            childNode.setParent(parentNode);
            parentNode.addChild(childNode);
        }
        private TreeNode get(MaterializedPath materializedPath, Action tag) {
            if( !index.containsKey(materializedPath) ) {
                int id = this.index.size();
                index.put( materializedPath, new TreeNode(id, materializedPath, tag) );
            }
            return index.get(materializedPath);
        }
        public TreeNode getRoot() {
            return this.root;
        }
    }
}
