package com.example.universalconverter.service;

import com.example.universalconverter.model.RequestObject;
import com.example.universalconverter.model.TestGraph;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterTest {

    private final Converter converter;

    ConverterTest() {
        converter = new ConverterImpl(new TestGraph());
    }

    @Test
    void convertTest() {
        List<RequestObject> actual = new ArrayList<>();
        actual.add(new RequestObject(" м ", " км * с / час"));
        actual.add(new RequestObject(" км / м ", ""));
        actual.add(new RequestObject(" с ", " час"));
        actual.add(new RequestObject("Ъ", "Э"));
        actual.add(new RequestObject("м", "час "));

        List<ResponseEntity<String>> expected = new ArrayList<>();
        expected.add(new ResponseEntity<>("3.6", HttpStatus.OK));
        expected.add(new ResponseEntity<>("1000", HttpStatus.OK));
        expected.add(new ResponseEntity<>("0.000277777777777778", HttpStatus.OK));
        expected.add(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        expected.add(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i), converter.convert(actual.get(i)));
        }
    }

}
