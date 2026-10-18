package com.example.foodsafety.model;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeRequest(
        @NotBlank String ingredients,
        String productName,
        String profileId
) {}
