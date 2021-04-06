package com.example.universalconverter.model;

public class RequestObject {

    private final String from;
    private final String to;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public RequestObject(String from, String to) {
        this.from = from;
        this.to = to;
    }

}