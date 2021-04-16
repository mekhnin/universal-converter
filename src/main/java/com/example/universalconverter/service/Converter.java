package com.example.universalconverter.service;

import com.example.universalconverter.model.RequestObject;
import org.springframework.http.ResponseEntity;

public interface Converter {

    ResponseEntity<String> convert(RequestObject object);

}