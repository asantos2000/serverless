package com.example.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;

public class WithConfig {
    private String greeting;

    @FnConfiguration
    public void setUp(RuntimeContext ctx) {
        greeting = ctx.getConfigurationByKey("GREETING").orElse("Hello");
    }

    public String handleRequest(String input) {
        String name = (input == null || input.isEmpty()) ? "world"  : input;
        return greeting + name + "!";
    }
}
