package fr.firmy.lab.eternity2server.model;

public class Node {

    private MaterializedPath path;
    private Action tag;

    public Node(MaterializedPath path, Action tag) {
        this.path = path;
        this.tag = tag;
    }

    public MaterializedPath getPath() {
        return this.path;
    }

    public Action getTag() {
        return this.tag;
    }

    public String toString() {
        return this.path.segmentsCount()>0 ? this.path.toString()+"."+this.tag.name() : this.tag.name() ;
    }

    public boolean equals(Object o) {
        boolean result = false;
        if( o instanceof Node) {
            result = ((Node)o).path.equals(this.path) && ((Node)o).tag.equals(this.tag);
        }
        return result;
    }

    public boolean isRoot() {
        return this.path.isRoot();
    }
}
