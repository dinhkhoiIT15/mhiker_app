package com.example.mhiker_app;

import java.io.Serializable;

public class Observation implements Serializable {
    private long id;
    private String observationText;
    private String timeOfObservation;
    private String additionalComments;
    private long hikeId;

    // THÊM MỚI: Trường cho đường dẫn ảnh
    private String imagePath;

    public Observation() {
    }

    // ... (Getters and setters cũ) ...

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getObservationText() {
        return observationText;
    }

    public void setObservationText(String observationText) {
        this.observationText = observationText;
    }

    public String getTimeOfObservation() {
        return timeOfObservation;
    }

    public void setTimeOfObservation(String timeOfObservation) {
        this.timeOfObservation = timeOfObservation;
    }

    public String getAdditionalComments() {
        return additionalComments;
    }

    public void setAdditionalComments(String additionalComments) {
        this.additionalComments = additionalComments;
    }

    public long getHikeId() {
        return hikeId;
    }

    public void setHikeId(long hikeId) {
        this.hikeId = hikeId;
    }

    // THÊM MỚI: Getter and Setter cho imagePath
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}