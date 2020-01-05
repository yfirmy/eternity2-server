package fr.firmy.lab.eternity2server.model;

import fr.firmy.lab.eternity2server.controller.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MaterializedPath implements Comparable<MaterializedPath> {

    private static Logger LOGGER = LoggerFactory.getLogger( MaterializedPath.class.getName() );

    private final List<String> segments;

    private static String SEPARATOR = ".";
    private static String piecePattern = "\\d{1,3}[WNES]";
    private static String materializedPathPattern = "(("+piecePattern+"\\"+SEPARATOR+")*("+piecePattern+"))?";

    public MaterializedPath(String path) throws MalformedMaterializedPathException {
        if( ! path.matches(materializedPathPattern) ) {
            throw new MalformedMaterializedPathException(path);
        }
        this.segments = path.equals("") ? new ArrayList<>() : Arrays.asList((String[]) path.split("\\."));
    }

    private MaterializedPath(List<String> path) {
        this.segments = new ArrayList<>(path);
    }

    public List<String> getSegments() {
        return new ArrayList<>(segments);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( segments.stream().collect(Collectors.joining( SEPARATOR )) );
        return  stringBuilder.toString();
    }

    public static Optional<MaterializedPath> build(String path) {
        Optional<MaterializedPath> result = Optional.empty();
        try {
            result = Optional.of(new MaterializedPath(path));
        } catch (MalformedMaterializedPathException e) {
            LOGGER.error("Impossible to build the MaterializedPath object", e);
        }
        return result;
    }

    public int segmentsCount() {
        return this.segments.size();
    }

    public boolean isRoot() {
        return this.segmentsCount()==0;
    }

    public Optional<MaterializedPath> getParent() {
        return getAncestor( this.segmentsCount() - 1 );
    }

    public Optional<MaterializedPath> getAncestor(int generation) {
        Optional<MaterializedPath> result = Optional.empty();
        if( this.segmentsCount() > 0 ) {
            LinkedList<String> segments = new LinkedList<>(this.getSegments().subList(0, generation));
            result = Optional.of(new MaterializedPath(segments));
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof MaterializedPath) && this.segments.equals(((MaterializedPath)other).getSegments());
    }

    @Override
    public int compareTo(MaterializedPath other) {
        int commonSegmentsCount = Math.min( this.segmentsCount(), other.segmentsCount() );
        for( int i=0; i<commonSegmentsCount; i++ ) {
            int compareSegment = this.getSegments().get(i).compareTo( other.getSegments().get(i) );
            if( compareSegment != 0 ) {
                return compareSegment;
            }
        }
        return this.segmentsCount() - other.segmentsCount();
    }
}