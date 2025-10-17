package se.fulkoping.rentalhub.controller;
// Paketet 'controller' innehåller alla klasser som tar emot och hanterar HTTP-anrop.
// Dessa klasser kallas "controllers" i Spring och fungerar som API:ets ingångspunkter.

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.fulkoping.rentalhub.dto.AssetDTO;
import se.fulkoping.rentalhub.model.AssetEntity;
import se.fulkoping.rentalhub.model.EntityMapper;
import se.fulkoping.rentalhub.service.RentalCoordinatorService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Denna klass hanterar alla API-anrop som rör tillgångar (utrustning).
 *
 * En "tillgång" kan t.ex. vara en borrmaskin, stege eller annan hyrbar utrustning.
 * Klassen fungerar som ett mellanlager mellan klienten (t.ex. frontend eller Swagger)
 * och backend-logiken som finns i `RentalCoordinatorService`.
 *
 * Alla metoder i denna klass returnerar JSON-svar.
 */
@RestController // Markerar att klassen är en REST-controller (hanterar HTTP-anrop och returnerar JSON)
@RequestMapping("/api/assets") // Alla metoder i klassen har denna URL som grund: /api/assets
@RequiredArgsConstructor // Skapar automatiskt en konstruktor för alla 'final'-fält (Lombok)
@Tag(name = "Assets", description = "API för hantering av utrustning och tillgångar")
// @Tag används av Swagger för att gruppera endpoints under fliken "Assets"
public class AssetController {

    // Fält som injiceras automatiskt av Spring via @RequiredArgsConstructor
    private final RentalCoordinatorService rentalService; // Hanterar logiken för uthyrning och tillgångar
    private final EntityMapper mapper; // Används för att omvandla mellan Entity- och DTO-objekt

    // ======================================================
    //                HÄMTA ALLA TILLGÅNGAR
    // ======================================================

    @GetMapping // Körs när någon anropar GET /api/assets
    public ResponseEntity<List<AssetDTO>> getAllAssets() {

        // Hämtar alla tillgångar från databasen via service-lagret.
        // Service returnerar en lista av AssetEntity-objekt.
        List<AssetDTO> dtoList = rentalService.listAllAssets()
                .stream() // Gör listan strömbaserad för att kunna bearbetas effektivt
                .map(mapper::toAssetDTO) // Konverterar varje AssetEntity till AssetDTO
                .collect(Collectors.toList()); // Samlar tillbaka till en lista

        // Skickar tillbaka listan till klienten med HTTP-status 200 OK.
        return ResponseEntity.ok(dtoList);
    }

    // ======================================================
    //           HÄMTA ENDAST TILLGÄNGLIGA TILLGÅNGAR
    // ======================================================

    @GetMapping("/available") // Körs när man anropar GET /api/assets/available
    public ResponseEntity<List<AssetDTO>> getAvailableAssets() {

        // Hämtar enbart de tillgångar som inte är uthyrda just nu.
        List<AssetDTO> dtoList = rentalService.listAvailableAssets()
                .stream()
                .map(mapper::toAssetDTO)
                .collect(Collectors.toList());

        // Returnerar listan över tillgängliga tillgångar.
        return ResponseEntity.ok(dtoList);
    }

    // ======================================================
    //                 SKAPA NY TILLGÅNG
    // ======================================================

    @PostMapping // Körs när man skickar POST /api/assets
    public ResponseEntity<?> createAsset(@Valid @RequestBody AssetEntity asset) {
        // @RequestBody betyder att JSON-datan i anropet konverteras till ett AssetEntity-objekt.
        // @Valid gör att fältvalidering (t.ex. @NotNull i modellen) kontrolleras automatiskt.

        try {
            // Skickar tillgången till service-lagret för att sparas i databasen.
            AssetEntity saved = rentalService.addAsset(asset);

            // Returnerar det sparade objektet som DTO med HTTP-status 201 (Created).
            return ResponseEntity.status(201).body(mapper.toAssetDTO(saved));
        } catch (Exception e) {
            // Om något går fel, t.ex. validering eller databasfel, returneras 400 (Bad Request)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    //                UPPDATERA TILLGÅNG
    // ======================================================

    @PutMapping("/{id}") // Körs när man skickar PUT /api/assets/{id}
    public ResponseEntity<?> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetEntity updatedAsset) {
        // @PathVariable betyder att {id} i URL:en kopplas till variabeln 'id'.
        // Exempel: PUT /api/assets/5 → id = 5
        // updatedAsset är den nya datan som ska ersätta den gamla.

        try {
            // Uppdaterar tillgången via service-lagret.
            AssetEntity saved = rentalService.updateAsset(id, updatedAsset);

            // Returnerar uppdaterat objekt som DTO.
            return ResponseEntity.ok(mapper.toAssetDTO(saved));
        } catch (Exception e) {
            // Vid fel returneras 400 Bad Request med felmeddelande.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    //                TA BORT TILLGÅNG
    // ======================================================

    @DeleteMapping("/{id}") // Körs när man skickar DELETE /api/assets/{id}
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        // Tar bort en tillgång utifrån dess ID.
        // Men i systemet finns ofta en regel att man inte får ta bort en tillgång
        // som är uthyrd eller har en aktiv bokning.

        try {
            rentalService.deleteAsset(id); // Försöker ta bort tillgången

            // Om det går bra skickas HTTP-status 204 (No Content) tillbaka,
            // vilket betyder att åtgärden lyckades men inget innehåll skickas.
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            // Om borttagning inte är tillåten (t.ex. tillgången är uthyrd)
            // returneras HTTP 409 (Conflict) med förklarande text.
            return ResponseEntity.status(409).body(e.getMessage());

        } catch (Exception e) {
            // Vid andra fel returneras HTTP 400 (Bad Request).
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
