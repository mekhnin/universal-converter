package com.example.universalconverter.controller;

import com.example.universalconverter.service.Converter;
import com.example.universalconverter.model.RequestObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConverterController {

    private final Converter converter;

    @Autowired
    public ConverterController(Converter converter) {
        this.converter = converter;
    }

    @PostMapping("/convert")
    public ResponseEntity<String> catchPostMapping(@RequestBody RequestObject object) {
        return converter.convert(object.getFrom(), object.getTo());
    }

}

