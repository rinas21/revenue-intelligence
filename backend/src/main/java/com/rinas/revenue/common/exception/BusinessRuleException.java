package com.rinas.revenue.common.exception;

import org.springframework.http.HttpStatus;

/**
 * The request is well-formed but violates a business rule (for example,
 * discounting an order below zero). Distinct from validation, which is about
 * the shape of the input.
 */
public class BusinessRuleException extends AppException {

    public BusinessRuleException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", message);
    }
}
