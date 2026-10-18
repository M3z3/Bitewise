package com.example.foodsafety.service;

import com.example.foodsafety.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FoodAnalysisService {
    // Conservative keyword rules. Ingredient text is not a substitute for a verified label or manufacturer confirmation.
    private static final Map<Allergen, List<String>> ALLERGEN_TERMS = Map.of(
        Allergen.PEANUT, List.of("peanut", "groundnut", "arachis oil"),
        Allergen.TREE_NUT, List.of("almond", "cashew", "walnut", "pecan", "pistachio", "hazelnut", "macadamia", "brazil nut", "pine nut"),
        Allergen.MILK, List.of("milk", "whey", "casein", "lactalbumin", "butter", "cream", "cheese", "yogurt", "ghee"),
        Allergen.EGG, List.of("egg", "albumin", "ovalbumin", "mayonnaise"),
        Allergen.WHEAT, List.of("wheat", "durum", "semolina", "spelt", "farina", "bulgur", "seitan"),
        Allergen.SOY, List.of("soy", "soya", "tofu", "tempeh", "edamame", "soy lecithin"),
        Allergen.FISH, List.of("salmon", "tuna", "cod", "anchovy", "sardine", "fish sauce", "tilapia"),
        Allergen.SHELLFISH, List.of("shrimp", "prawn", "crab", "lobster", "crayfish", "clam", "mussel", "oyster", "scallop"),
        Allergen.SESAME, List.of("sesame", "tahini", "benne")
    );

    private static final Map<DietaryRestriction, List<String>> RESTRICTION_TERMS = Map.of(
        DietaryRestriction.VEGAN, List.of("beef", "pork", "chicken", "turkey", "lamb", "fish", "shrimp", "gelatin", "honey", "milk", "whey", "casein", "butter", "cheese", "egg", "albumin"),
        DietaryRestriction.VEGETARIAN, List.of("beef", "pork", "chicken", "turkey", "lamb", "fish", "shrimp", "gelatin", "anchovy", "fish sauce"),
        DietaryRestriction.PESCATARIAN, List.of("beef", "pork", "chicken", "turkey", "lamb", "gelatin"),
        DietaryRestriction.GLUTEN_FREE, List.of("wheat", "barley", "rye", "malt", "spelt", "semolina", "bulgur", "farro", "triticale", "seitan"),
        DietaryRestriction.DAIRY_FREE, List.of("milk", "whey", "casein", "butter", "cream", "cheese", "yogurt", "ghee", "lactalbumin"),
        DietaryRestriction.EGG_FREE, List.of("egg", "albumin", "ovalbumin", "mayonnaise"),
        DietaryRestriction.SOY_FREE, List.of("soy", "soya", "tofu", "tempeh", "edamame"),
        DietaryRestriction.LOW_SODIUM, List.of("salt", "sodium", "monosodium glutamate", "msg", "brine", "sodium bicarbonate"),
        DietaryRestriction.KOSHER, List.of("pork", "bacon", "ham", "lard", "shellfish", "shrimp", "crab", "lobster"),
        DietaryRestriction.HALAL, List.of("pork", "bacon", "ham", "lard", "wine", "beer", "rum", "brandy")
    );

    public AnalysisResponse analyze(AnalyzeRequest request, UserFoodProfile profile) {
        String text = normalize(request.ingredients());
        Set<String> ingredientTokens = Arrays.stream(text.split("[,;\\n]+"))
                .map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> detectedAllergens = new ArrayList<>();
        List<String> matchedIngredients = new ArrayList<>();
        for (Allergen allergen : Allergen.values()) {
            List<String> terms = ALLERGEN_TERMS.getOrDefault(allergen, List.of());
            List<String> matches = findMatches(text, terms);
            if (!matches.isEmpty()) {
                detectedAllergens.add(allergen.name());
                matchedIngredients.addAll(matches.stream().map(m -> allergen.name() + ": " + m).toList());
            }
        }

        List<String> conflicts = new ArrayList<>();
        if (profile != null) {
            for (Allergen allergen : profile.allergies()) {
                List<String> terms = ALLERGEN_TERMS.getOrDefault(allergen, List.of());
                List<String> matches = findMatches(text, terms);
                if (!matches.isEmpty()) {
                    conflicts.add("ALLERGY " + allergen.name() + " matched: " + String.join(", ", matches));
                }
            }
            for (DietaryRestriction restriction : profile.restrictions()) {
                List<String> matches = findMatches(text, RESTRICTION_TERMS.getOrDefault(restriction, List.of()));
                if (!matches.isEmpty()) {
                    conflicts.add(restriction.name() + " possible conflict: " + String.join(", ", matches));
                }
            }
        }

        List<String> uncertainties = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("may contain") || lower.contains("shared equipment") || lower.contains("cross-contamination")) {
            uncertainties.add("Label mentions possible cross-contact. Treat this as unresolved for an allergy.");
        }
        if (lower.contains("natural flavors") || lower.contains("spices") || lower.contains("seasoning") || lower.contains("modified food starch")) {
            uncertainties.add("A broad ingredient term appears. Its source may need confirmation from the manufacturer.");
        }
        if (lower.contains("gluten-free") || lower.contains("certified") || lower.contains("kosher") || lower.contains("halal")) {
            uncertainties.add("Certification or claim wording was not independently verified.");
        }
        if (ingredientTokens.isEmpty()) {
            uncertainties.add("No usable ingredient entries were detected.");
        }
        uncertainties.add("Keyword matching can miss synonyms, derivatives, ambiguous ingredients, and cross-contact. Verify the package label and contact the manufacturer for severe allergies.");

        String status = conflicts.stream().anyMatch(s -> s.startsWith("ALLERGY ")) ? "POTENTIAL_ALLERGEN_CONFLICT"
                : !conflicts.isEmpty() ? "DIETARY_CONFLICT"
                : !detectedAllergens.isEmpty() ? "ALLERGENS_DETECTED"
                : "NO_MATCH_FOUND";
        return new AnalysisResponse(
                request.productName() == null || request.productName().isBlank() ? "Unnamed product" : request.productName(),
                status,
                List.copyOf(detectedAllergens),
                List.copyOf(conflicts),
                matchedIngredients.stream().distinct().toList(),
                List.copyOf(uncertainties),
                "Results are based only on the ingredient text supplied. NO_MATCH_FOUND does not mean the product is safe."
        );
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private static List<String> findMatches(String text, List<String> terms) {
        List<String> found = new ArrayList<>();
        for (String term : terms) {
            Pattern p = Pattern.compile("(?<![a-z])" + Pattern.quote(term) + "(?![a-z])", Pattern.CASE_INSENSITIVE);
            if (p.matcher(text).find()) found.add(term);
        }
        return found;
    }
}
