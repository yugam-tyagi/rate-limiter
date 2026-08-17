package com.yugam.ratelimiter.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Map;

@Getter
public class PolicyData {
    @NotNull(message = "Policy data can't be null.")
    @NotEmpty(message = "Policy data can't be empty.")
    private Map<String,Object> data;
}
