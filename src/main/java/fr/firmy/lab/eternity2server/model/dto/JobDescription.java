package fr.firmy.lab.eternity2server.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.firmy.lab.eternity2server.model.dto.serializer.JobDescriptionDeserializer;
import fr.firmy.lab.eternity2server.model.dto.serializer.JobDescriptionSerializer;

@JsonDeserialize(using = JobDescriptionDeserializer.class)
@JsonSerialize(using = JobDescriptionSerializer.class)
public class JobDescription {

    private BoardDescription job;

    public JobDescription(BoardDescription description) {
        this.job = description;
    }

    public BoardDescription getJob() {
        return this.job;
    }

    public void setJob(BoardDescription job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return this.job.toString();
    }

}
