package com.example.resumeoback.candidate;

public class Candidate {
    private String id;
    private String name;
    private int score;
    private String date;
    private String appointment;
    private String competences;
    private String standout;
    private String pros;
    private String cons;

    public Candidate() {}

    public Candidate(String id, String name, int score, String date, String appointment) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.date = date;
        this.appointment = appointment;


        this.competences = "Brak danych";
        this.standout = "Brak danych";
        this.pros = "Brak danych";
        this.cons = "Brak danych";


    }

    public Candidate(String id, String name, int score, String date, String appointment,String competences, String standout, String pros, String cons) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.date = date;
        this.appointment = appointment;

        this.competences = competences;
        this.standout = standout;
        this.pros = pros;
        this.cons = cons;
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

    public String getCompetences() { return competences; }
    public void setCompetences(String competences) { this.competences = competences; }

    public String getStandout() { return standout; }
    public void setStandout(String standout) { this.standout = standout; }

    public String getPros() { return pros; }
    public void setPros(String pros) { this.pros = pros; }

    public String getCons() { return cons; }
    public void setCons(String cons) { this.cons = cons; }

}