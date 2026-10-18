package com.example.foodsafety;

import com.example.foodsafety.model.*;
import com.example.foodsafety.service.FoodAnalysisService;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class FoodAnalysisServiceTest {
    private final FoodAnalysisService service = new FoodAnalysisService();

    @Test
    void detectsPeanutAllergyConflict() {
        var profile = new UserFoodProfile("u1", "Test", Set.of(Allergen.PEANUT), Set.of());
        var response = service.analyze(new AnalyzeRequest("Rice, peanut oil, salt", "Snack", "u1"), profile);
        assertEquals("POTENTIAL_ALLERGEN_CONFLICT", response.status());
        assertTrue(response.detectedAllergens().contains("PEANUT"));
        assertTrue(response.restrictionConflicts().stream().anyMatch(s -> s.contains("PEANUT")));
    }

    @Test
    void detectsVeganConflict() {
        var profile = new UserFoodProfile("u2", "Test", Set.of(), Set.of(DietaryRestriction.VEGAN));
        var response = service.analyze(new AnalyzeRequest("Oats, honey, cinnamon", "Cereal", "u2"), profile);
        assertEquals("DIETARY_CONFLICT", response.status());
    }

    @Test
    void noMatchDoesNotClaimSafe() {
        var response = service.analyze(new AnalyzeRequest("rice, water", "Rice", null), null);
        assertEquals("NO_MATCH_FOUND", response.status());
        assertTrue(response.explanation().contains("does not mean the product is safe"));
    }
}
