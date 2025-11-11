package com.example.mhiker_app;

import java.io.Serializable;

public class Hike implements Serializable {
    private long id;
    private String name;
    private String location;
    private String dateOfHike;
    private boolean parkingAvailable;
    private String lengthOfHike;
    private String difficultyLevel;
    private String description;
    private String hikerCount;
    private String equipment;

    public Hike() {
    }

    public Hike(long id, String name, String location, String dateOfHike, boolean parkingAvailable, String lengthOfHike, String difficultyLevel, String description, String hikerCount, String equipment) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.dateOfHike = dateOfHike;
        this.parkingAvailable = parkingAvailable;
        this.lengthOfHike = lengthOfHike;
        this.difficultyLevel = difficultyLevel;
        this.description = description;
        this.hikerCount = hikerCount;
        this.equipment = equipment;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDateOfHike() {
        return dateOfHike;
    }

    public void setDateOfHike(String dateOfHike) {
        this.dateOfHike = dateOfHike;
    }

    public boolean isParkingAvailable() {
        return parkingAvailable;
    }

    public void setParkingAvailable(boolean parkingAvailable) {
        this.parkingAvailable = parkingAvailable;
    }

    public String getLengthOfHike() {
        return lengthOfHike;
    }

    public void setLengthOfHike(String lengthOfHike) {
        this.lengthOfHike = lengthOfHike;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHikerCount() {
        return hikerCount;
    }

    public void setHikerCount(String hikerCount) {
        this.hikerCount = hikerCount;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
}