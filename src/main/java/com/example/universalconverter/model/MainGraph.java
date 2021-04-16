package com.example.universalconverter.model;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MainGraph extends Graph {

    public MainGraph(ApplicationArguments args) throws IOException {
        Path pathToCsvFile = Path.of(args.getSourceArgs()[0]);
        Files.lines(pathToCsvFile).forEach(this::addLine);
    }

}