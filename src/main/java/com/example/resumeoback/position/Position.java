package com.example.resumeoback.position;

public class Position {
    private String id;
    private String name;
    private int number_of_candidates;
    private String date;

    public Position() {}

    public Position(String id, String name, int number_of_candidates, String date) {
        this.id = id;
        this.name = name;
        this.number_of_candidates = number_of_candidates;
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getNumber_of_candidates() { return number_of_candidates; }
    public void setNumber_of_candidates(int number_of_candidates) {
        this.number_of_candidates = number_of_candidates;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}