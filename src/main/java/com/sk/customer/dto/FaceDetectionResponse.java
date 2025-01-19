package com.sk.customer.dto;

public record FaceDetectionResponse(boolean faceDetected,
                                    String rawResponse) {
}
