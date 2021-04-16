package com.example.universalconverter.model;

import java.util.HashMap;
import java.util.Map;

public abstract class Graph {

    private final Map<String, Map<String, Double>> edges = new HashMap<>();

    protected void addLine(String line) {
        String[] data = line.split(",");
        edges.putIfAbsent(data[0], new HashMap<>());
        edges.putIfAbsent(data[1], new HashMap<>());
        double rate = Double.parseDouble(data[2]);
        edges.get(data[0]).put(data[1], rate);
        edges.get(data[1]).put(data[0], 1 / rate);
    }

    public Map<String, Double> getBonds(String node) {
        return edges.get(node);
    }

}