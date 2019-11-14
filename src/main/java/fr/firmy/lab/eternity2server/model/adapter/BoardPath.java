package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.model.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class BoardPath {

    private static Logger LOGGER = LoggerFactory.getLogger( BoardPath.class.getName() );
    private List<Coordinates> path;
    private int width;

    @Autowired
    public BoardPath(ServerConfiguration serverConfiguration) {
        this.width = (int)Math.sqrt(serverConfiguration.getBoardSize());
        fillPath();
    }

    private void fillPath() {
        int N = this.width - 1;
        this.path = new ArrayList<>(this.width * this.width);

        this.path.add(new Coordinates(0,0));
        this.path.add(new Coordinates( N,0));
        this.path.add(new Coordinates(0, N));
        this.path.add(new Coordinates( N, N));

        // ring 0
        fillRing0( N);

        // ring 1 to N
        for(int ringIdx=1; ringIdx<=this.width/2; ringIdx++) {
            fillRing(ringIdx, N);
        }
    }

    private Coordinates fillRingBorder(Coordinates origin,
                                int ringBorderSize,
                                Direction direction) {

        if( direction == Direction.EAST ) {
            IntStream.range( 0, ringBorderSize ).mapToObj(dx->new Coordinates(origin.x + dx, origin.y)).forEach(path::add);
        }
        if( direction == Direction.SOUTH ) {
            IntStream.range( 0, ringBorderSize ).mapToObj(dy->new Coordinates(origin.x, origin.y + dy)).forEach(path::add);
        }
        if( direction == Direction.WEST ) {
            IntStream.range( 0, ringBorderSize ).mapToObj(dx->new Coordinates(origin.x - dx, origin.y)).forEach(path::add);
        }
        if( direction == Direction.NORTH ) {
            IntStream.range( 0, ringBorderSize ).mapToObj(dy->new Coordinates(origin.x, origin.y - dy)).forEach(path::add);
        }

        Coordinates next = path.remove(path.size()-1);

        LOGGER.debug(origin.toString() + "-> "+direction.name()+"["+ringBorderSize+"]:"+this.toString());

        return next;
    }

    private void fillRing0(int N) {
        LOGGER.debug("Ring 0");
        this.fillRingBorder( new Coordinates( 1, 0 ), N, Direction.EAST);
        this.fillRingBorder( new Coordinates( N, 1 ), N, Direction.SOUTH);
        this.fillRingBorder( new Coordinates( N-1, N ), N, Direction.WEST);
        this.fillRingBorder( new Coordinates( 0, N-1 ), N, Direction.NORTH);
    }

    private void fillLastPiece(int ringIdx, List<Coordinates> path) {
        if( path.size() == width*width - 1 ) {
            path.add(new Coordinates(ringIdx, ringIdx));
        }
    }

    private void fillRing(int ringIdx, int N) {
        int ringBorderSize = N - (2 * ringIdx) + 1 ;
        LOGGER.debug("Ring "+ringIdx+" ringBorderSize="+ringBorderSize);
        if( ringBorderSize > 1 ) {
            this.fillRingBorder(
                this.fillRingBorder(
                    this.fillRingBorder(
                        this.fillRingBorder(
                                new Coordinates( ringIdx, ringIdx ),
                                ringBorderSize, Direction.EAST),
                            ringBorderSize, Direction.SOUTH),
                        ringBorderSize, Direction.WEST),
                    ringBorderSize, Direction.NORTH);
        } else {
            fillLastPiece(ringIdx, path);
        }
    }

    public Stream<Coordinates> stream() {
        return this.path.stream();
    }

    public Coordinates get(int i) {
        return this.path.get(i);
    }

    public String toString() {
        return path.stream().map(Coordinates::toString).collect(Collectors.joining(" "));
    }

    class Coordinates {
        int x;
        int y;
        Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public String toString() {
            return "("+x+","+y+")";
        }
    }

}
