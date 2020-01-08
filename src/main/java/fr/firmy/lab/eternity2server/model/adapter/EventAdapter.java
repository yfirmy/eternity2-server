package fr.firmy.lab.eternity2server.model.adapter;

import fr.firmy.lab.eternity2server.controller.exception.MalformedEventDescriptionException;
import fr.firmy.lab.eternity2server.model.Event;
import fr.firmy.lab.eternity2server.model.dto.EventDescription;
import org.springframework.stereotype.Component;

@Component
public class EventAdapter {

    public EventDescription toDescription(Event event) {
        return new EventDescription(
                event.getSolverName(),
                event.getStatus().name());
    }

    public Event fromDescription(EventDescription eventDescription) throws MalformedEventDescriptionException {
        Event result;
        try {
            result = new Event( eventDescription.getSolverName(), Event.Status.parseStatus(eventDescription.getStatus()) );
        }
        catch(IllegalArgumentException e) {
            throw new MalformedEventDescriptionException(eventDescription, e);
        }
        return result;
    }

}