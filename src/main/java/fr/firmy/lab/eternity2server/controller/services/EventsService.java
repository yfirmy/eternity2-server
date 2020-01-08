package fr.firmy.lab.eternity2server.controller.services;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.dal.WorkersTimeline;
import fr.firmy.lab.eternity2server.model.Event;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventsService {

    private final WorkersTimeline timeline;

    @Autowired
    public EventsService(WorkersTimeline timeline) {
        this.timeline = timeline;
    }

    public void publishEvent(Event event) {
        switch (event.getStatus()) {

            case STARTED:
                timeline.eventSolverStarted( event.getSolverName() );
                break;
            case REQUESTING:
                timeline.eventSolverRequesting( event.getSolverName() );
                break;
            case WAITING:
                timeline.eventSolverWaiting( event.getSolverName() );
                break;
            case SOLVING:
                timeline.eventSolverSolving( event.getSolverName() );
                break;
            case REPORTING:
                timeline.eventSolverReporting( event.getSolverName() );
                break;
            case STOPPED:
                timeline.eventSolverStopped( event.getSolverName() );
                break;
        }
    }

}
