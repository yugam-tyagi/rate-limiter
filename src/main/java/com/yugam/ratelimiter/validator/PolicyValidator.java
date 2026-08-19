package com.yugam.ratelimiter.validator;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.exceptions.InvalidPolicyException;
import com.yugam.ratelimiter.model.PolicyData;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PolicyValidator {

    public void validate(AlgorithmType algorithmType, PolicyData policyData) {

        if (policyData.getData() == null || policyData.getData().isEmpty()) {
            throw new InvalidPolicyException("Policy data can't be empty.");
        }

        Map<String, Object> data = policyData.getData();

        switch (algorithmType) {
            case FIXED_WINDOW, SLIDING_WINDOW ->
                    validateWindowPolicy(data);

            case TOKEN_BUCKET ->
                    validateTokenBucketPolicy(data);

            case LEAKY_BUCKET ->
                    validateLeakyBucketPolicy(data);
        }
    }

    private void validateWindowPolicy(Map<String, Object> data) {
        validateFields(data, "maxRequests", "windowDuration");
        validatePositiveNumber(data, "maxRequests");
        validatePositiveNumber(data, "windowDuration");
    }

    private void validateTokenBucketPolicy(Map<String, Object> data) {
        validateFields(data, "bucketCapacity", "refillRate");
        validatePositiveNumber(data, "bucketCapacity");
        validatePositiveNumber(data, "refillRate");
    }

    private void validateLeakyBucketPolicy(Map<String, Object> data) {
        validateFields(data, "bucketCapacity", "leakRate");
        validatePositiveNumber(data, "bucketCapacity");
        validatePositiveNumber(data, "leakRate");
    }

    private void validateFields(Map<String, Object> data, String... allowedFields) {
        for (String field : data.keySet()) {
            boolean allowed = false;

            for (String allowedField : allowedFields) {
                if (allowedField.equals(field)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                throw new InvalidPolicyException(
                        "Unknown policy field: " + field
                );
            }
        }
    }

    private void validatePositiveNumber(Map<String, Object> data, String fieldName) {
        Object value = data.get(fieldName);

        if (!(value instanceof Number) || ((Number) value).longValue() <= 0) {
            throw new InvalidPolicyException(
                    fieldName + " must be greater than 0."
            );
        }
    }
}