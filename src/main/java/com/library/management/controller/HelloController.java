package com.library.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Simple health/hello endpoints")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Hello check", description = "Returns a simple message confirming the API is running")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Library Management System is running!",
                "status", "ok"
        );
    }
}
