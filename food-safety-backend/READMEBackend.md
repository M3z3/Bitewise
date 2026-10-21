# Food Safety Backend (Java / Spring Boot)

REST backend that compares supplied ingredient text against a user profile containing allergies and dietary restrictions.

## Requirements
- Java 25 (latest LTS)
- Maven 3.9+ (or use your IDE's Maven integration)

## Run
```bash
mvn spring-boot:run
```
The API starts at `http://localhost:8080`.

Run tests:
```bash
mvn test
```

## API

### Create or update a profile
`POST /api/profiles`

```json
{
  "id": "profile-1",
  "name": "Rohan",
  "allergies": ["PEANUT", "MILK"],
  "restrictions": ["VEGAN", "GLUTEN_FREE"]
}
```

Supported allergies: `PEANUT`, `TREE_NUT`, `MILK`, `EGG`, `WHEAT`, `SOY`, `FISH`, `SHELLFISH`, `SESAME`.

Supported restrictions: `VEGAN`, `VEGETARIAN`, `PESCATARIAN`, `GLUTEN_FREE`, `DAIRY_FREE`, `EGG_FREE`, `SOY_FREE`, `LOW_SODIUM`, `KOSHER`, `HALAL`.

### List, read, delete profiles
- `GET /api/profiles`
- `GET /api/profiles/{id}`
- `DELETE /api/profiles/{id}`

### Analyze ingredients
`POST /api/analyze`

```json
{
  "productName": "Snack bar",
  "ingredients": "Oats, peanut butter, whey protein, salt. May contain sesame.",
  "profileId": "profile-1"
}
```

`profileId` is optional. Without it, the service still reports detected allergen keywords but does not compare against a saved profile.

Example response:
```json
{
  "productName": "Snack bar",
  "status": "POTENTIAL_ALLERGEN_CONFLICT",
  "detectedAllergens": ["PEANUT", "MILK", "SESAME"],
  "restrictionConflicts": [
    "ALLERGY PEANUT matched: peanut",
    "ALLERGY MILK matched: whey",
    "VEGAN possible conflict: peanut, whey"
  ],
  "matchedIngredients": ["PEANUT: peanut", "MILK: whey", "SESAME: sesame"],
  "uncertainties": [
    "Label mentions possible cross-contact. Treat this as unresolved for an allergy.",
    "Keyword matching can miss synonyms, derivatives, ambiguous ingredients, and cross-contact. Verify the package label and contact the manufacturer for severe allergies."
  ],
  "explanation": "Results are based only on the ingredient text supplied. NO_MATCH_FOUND does not mean the product is safe."
}
```

## Status values
- `POTENTIAL_ALLERGEN_CONFLICT`: a configured allergy keyword matched.
- `DIETARY_CONFLICT`: at least one configured dietary restriction keyword matched.
- `ALLERGENS_DETECTED`: allergen terms were detected, but no profile conflict was found.
- `NO_MATCH_FOUND`: no configured keyword matched. This is not a safety guarantee.

## Included functionality
- Create/update, list, retrieve, and delete food profiles
- Allergen keyword detection
- Restriction conflict detection
- Explicit uncertainty notes for cross-contact and ambiguous ingredient terms
- Request validation and consistent HTTP errors
- Unit tests for core analysis behavior

## Important implementation limits
This starter backend uses an in-memory profile store, so profiles disappear when the app restarts. Add a database and authentication before deploying. The rules are intentionally basic and are not a certified food-safety system. It does not read barcode data, scan images, validate certifications, or establish that a food is safe. The ingredient list must be complete and accurate. For serious allergies, check the package's allergen statement and contact the manufacturer; do not rely on a keyword result alone.

## Suggested production additions
1. PostgreSQL persistence and schema migrations.
2. Authentication and per-user authorization.
3. Audit logging with privacy controls and rate limiting.
4. Barcode lookup integration and OCR for package labels.
5. A reviewed ingredient ontology, multilingual synonyms, and automated regression tests.
6. Integration tests and deployment configuration.
