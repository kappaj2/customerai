package com.sk.customer.dto;

public record ProcessResponse(
        String body,
        String message,
        ReadResponse responseDetails
) {
}
