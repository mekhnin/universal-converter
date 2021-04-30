package com.example.universalconverter.controller;

import com.example.universalconverter.service.Converter;
import com.example.universalconverter.model.RequestObject;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

    @ApiOperation(value = "${ConverterController.convert:Converts units of measurement}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "There is a conversion factor in the response body", response = String.class),
            @ApiResponse(code = 400, message = "The request expressions use unknown units"),
            @ApiResponse(code = 404, message = "The conversion is not possible")})
    @PostMapping("/convert")
    public ResponseEntity<String> convert(
            @ApiParam(name = "object", value = "Units of measurement for converting")
            @RequestBody RequestObject object) {
        return converter.convert(object);
    }

}