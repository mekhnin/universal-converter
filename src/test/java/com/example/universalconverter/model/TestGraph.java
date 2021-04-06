package com.example.universalconverter.model;

import java.util.Arrays;


public class TestGraph extends Graph {

    private static final String RULES =
            "м,см,100\n" +
            "мм,м,0.001\n" +
            "км,м,1000\n" +
            "час,мин,60\n" +
            "мин,с,60";

    public TestGraph() {
        Arrays.stream(RULES.split("\n")).forEach(this::addLine);
    }

}
