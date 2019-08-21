package fr.firmy.lab.eternity2server.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class Solution {

    private static Logger LOGGER = LoggerFactory.getLogger( Solution.class );

    private MaterializedPath path;
    private Date dateSolved;

    public Solution(MaterializedPath path, Date dateSolved) {
        this.path = path;
        this.dateSolved = dateSolved;
    }

    public MaterializedPath getPath() {
        return this.path;
    }

    public Date getDateSolved() {
        return this.dateSolved;
    }

    @Override
    public String toString() {
        return this.path.toString() + " [" + this.dateSolved.toString() + "]";
    }
}