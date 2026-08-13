package com.yugam.ratelimiter.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class PolicyData {
    private Map<String,Object> data;
}
