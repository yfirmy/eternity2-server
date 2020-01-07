package fr.firmy.lab.eternity2server.controller.dal.impl;

import fr.firmy.lab.eternity2server.configuration.ServerConfiguration;
import fr.firmy.lab.eternity2server.controller.dal.WorkersTimeline;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WorkersTimelineImpl implements WorkersTimeline {

    private InfluxDB influxDB;

    @Autowired
    public WorkersTimelineImpl(ServerConfiguration serverConfiguration) {
        this.influxDB = InfluxDBFactory.connect(serverConfiguration.getInfluxDbUrl());
        this.influxDB.enableBatch(100, 200, TimeUnit.MILLISECONDS);
        this.influxDB.setRetentionPolicy(serverConfiguration.getInfluxDbRetentionPolicy());
        this.influxDB.setDatabase(serverConfiguration.getInfluxDbName());
    }

    private void sendEvent(String solver_name, Status status) {

        Point point = Point.measurement("solver_status")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("solver_name", solver_name)
                .addField("value", status.name())
                .build();

        this.influxDB.write( point );
    }

    @Override
    public void eventSolverStarted(String solver_name) {
        sendEvent(solver_name, Status.STARTED);
    }

    @Override
    public void eventSolverSolving(String solver_name) {
        sendEvent(solver_name, Status.SOLVING);
    }

    @Override
    public void eventSolverIdle(String solver_name) {
        sendEvent(solver_name, Status.IDLE);
    }

    @Override
    public void eventSolverStopped(String solver_name) {
        sendEvent(solver_name, Status.STOPPED);
    }


    private enum Status {
        STARTED,
        IDLE,
        SOLVING,
        STOPPED
    }
}
