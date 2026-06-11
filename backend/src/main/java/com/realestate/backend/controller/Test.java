package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class Test {
    @GetMapping
    public ApiResponse<String> test(){
        return ResponseEntity.ok(ApiResponse.success("Data fetched successfully", "This is test")).
                getBody();
    }
}
