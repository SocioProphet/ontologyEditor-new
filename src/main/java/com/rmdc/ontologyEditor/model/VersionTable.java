package com.rmdc.ontologyEditor.model;

public class VersionTable {
    int id;
    private String number;
    private String prior;
    private String description;
    private String time;

    public VersionTable() {
    }

    public VersionTable(int id, String number, String prior, String description, String time) {
        this.id = id;
        this.number = number;
        this.prior = prior;
        this.description = description;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPrior() {
        return prior;
    }

    public void setPrior(String prior) {
        this.prior = prior;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
