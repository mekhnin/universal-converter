package com.example.universalconverter.service;

import org.springframework.http.ResponseEntity;

public interface Converter {

    ResponseEntity<String> convert(String from, String to);

}
