package com.example.foodsafety.model;

import java.util.List;

public record AnalysisResponse(
        String productName,
        String status,
        List<String> detectedAllergens,
        List<String> restrictionConflicts,
        List<String> matchedIngredients,
        List<String> uncertainties,
        String explanation
) {}
