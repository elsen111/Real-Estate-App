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
    public ResponseEntity<ApiResponse<String>> test() {
        ApiResponse<String> response = ApiResponse.success("Data fetched successfully", "This is test");
        return ResponseEntity.ok(response);
    }
}
