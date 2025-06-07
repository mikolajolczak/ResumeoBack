package com.example.resumeoback.candidate;

public class Candidate {
    private String id;
    private String name;
    private int score;
    private String date;
    private String appointment;

    public Candidate() {}

    public Candidate(String id, String name, int score, String date, String appointment) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.date = date;
        this.appointment = appointment;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getAppointment() { return appointment; }
    public void setAppointment(String appointment) { this.appointment = appointment; }
}