package se.fulkoping.rentalhub.controller;
// Klassen ligger i paketet 'controller' där alla REST-API-kontrollers finns.
// En controller tar emot HTTP-anrop (t.ex. GET, POST, PUT, DELETE) och skickar
// vidare logiken till service-lagret (i detta fall RentalCoordinatorService).

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.fulkoping.rentalhub.dto.BookingDTO;
import se.fulkoping.rentalhub.model.BookingEntity;
import se.fulkoping.rentalhub.model.EntityMapper;
import se.fulkoping.rentalhub.service.RentalCoordinatorService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Denna klass hanterar allt som rör uthyrning, återlämning och historik av bokningar.
 *
 * Den fungerar som ett gränssnitt mellan klienten (frontend eller Swagger)
 * och applikationens affärslogik som ligger i `RentalCoordinatorService`.
 *
 * Klassen använder `EntityMapper` för att konvertera mellan databasobjekt (Entity)
 * och enklare dataöverföringsobjekt (DTO) som skickas till/från API:et.
 */
@RestController // Gör klassen till en REST-controller som returnerar JSON-svar
@RequestMapping("/api/rentals") // Bas-URL för alla endpoints i denna controller
@RequiredArgsConstructor // Lombok-annotation: skapar automatiskt konstruktor för 'final'-fälten
@Tag(name = "Rentals", description = "API för bokningar, återlämning och historik")
// @Tag används av Swagger för att visa dessa endpoints i gruppen "Rentals"
public class RentalController {

    // ======================================================
    //          FÄLT (AUTOMATISKT INJEKTERADE AV SPRING)
    // ======================================================

    private final RentalCoordinatorService rentalService; // Hanterar logiken för bokningar och uthyrningar
    private final EntityMapper mapper; // Konverterar mellan BookingEntity och BookingDTO

    // ======================================================
    //                SKAPA NY BOKNING
    // ======================================================

    @PostMapping("/book/{assetId}/customer/{customerId}")
    // Denna metod anropas när klienten skickar ett POST-anrop till:
    // /api/rentals/book/{assetId}/customer/{customerId}
    public ResponseEntity<?> createBooking(
            @PathVariable Long assetId,   // Tillgångens ID (t.ex. borrmaskinens ID)
            @PathVariable Long customerId, // Kundens ID som gör bokningen
            @Valid @RequestBody(required = false) BookingDTO request) {
        // @RequestBody tar emot JSON-data i anropet (t.ex. datum, kommentar)
        // 'required = false' gör att kroppen inte måste skickas (den kan vara tom).
        // @Valid säkerställer att eventuell data följer valideringsreglerna.

        try {
            // Skickar bokningsuppgifterna vidare till service-lagret för hantering.
            BookingEntity saved = rentalService.registerBooking(assetId, customerId, request);

            // Returnerar det sparade objektet som DTO med HTTP-status 201 (Created).
            return ResponseEntity.status(201).body(mapper.toBookingDTO(saved));

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Fångar vanliga fel, t.ex. ogiltiga ID:n eller otillgänglig tillgång.
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            // Fångar oväntade fel och returnerar ett generiskt serverfel (500).
            return ResponseEntity.internalServerError().body("Ett oväntat fel inträffade.");
        }
    }

    // ======================================================
    //                HÄMTA ALLA BOKNINGAR
    // ======================================================

    @GetMapping // GET /api/rentals
    public ResponseEntity<List<BookingDTO>> getAllBookings() {

        // Hämtar alla bokningar från databasen via service-lagret.
        List<BookingDTO> dtoList = rentalService.listAllBookings()
                .stream() // Använder Java Stream API för att bearbeta listan
                .map(mapper::toBookingDTO) // Konverterar varje BookingEntity till BookingDTO
                .collect(Collectors.toList()); // Samlar ihop dem till en lista

        // Returnerar listan med status 200 (OK).
        return ResponseEntity.ok(dtoList);
    }

    // ======================================================
    //                HÄMTA EN BOKNING VIA ID
    // ======================================================

    @GetMapping("/{id}") // GET /api/rentals/{id}
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            // Hämtar bokningen via dess unika ID.
            BookingEntity booking = rentalService.getBookingById(id);

            // Om hittad – returnera bokningen som DTO med 200 OK.
            return ResponseEntity.ok(mapper.toBookingDTO(booking));
        } catch (Exception e) {
            // Om bokningen inte hittas returneras 404 (Not Found).
            return ResponseEntity.notFound().build();
        }
    }

    // ======================================================
    //                MARKERA SOM ÅTERLÄMNAD
    // ======================================================

    @PutMapping("/return/{bookingId}") // PUT /api/rentals/return/{bookingId}
    public ResponseEntity<?> markReturned(@PathVariable Long bookingId) {
        try {
            // Anropar service-lagret för att avsluta bokningen och markera den som återlämnad.
            BookingEntity updated = rentalService.closeBooking(bookingId);

            // Returnerar uppdaterad bokning som DTO.
            return ResponseEntity.ok(mapper.toBookingDTO(updated));

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Vid t.ex. ogiltigt ID eller försök att återlämna redan stängd bokning → 400.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    //                  TA BORT EN BOKNING
    // ======================================================

    @DeleteMapping("/{id}") // DELETE /api/rentals/{id}
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            // Försöker ta bort en bokning via service-lagret.
            rentalService.deleteBooking(id);

            // Returnerar 204 (No Content) om borttagningen lyckas.
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            // Om bokningen inte får tas bort (t.ex. aktiv eller inte återlämnad) → 409 Conflict.
            return ResponseEntity.status(409).body(e.getMessage());

        } catch (Exception e) {
            // Andra fel (t.ex. databasfel) → 400 Bad Request.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    //             HÄMTA HISTORIK FÖR EN TILLGÅNG
    // ======================================================

    @GetMapping("/history/asset/{assetId}") // GET /api/rentals/history/asset/{assetId}
    public ResponseEntity<List<BookingDTO>> getAssetHistory(@PathVariable Long assetId) {

        // Hämtar historik (alla bokningar) kopplade till en viss tillgång.
        List<BookingDTO> dtoList = rentalService.getBookingHistoryForAsset(assetId)
                .stream()
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());

        // Returnerar historiken som lista av DTO-objekt.
        return ResponseEntity.ok(dtoList);
    }

    // ======================================================
    //             HÄMTA HISTORIK FÖR EN KUND
    // ======================================================

    @GetMapping("/history/customer/{customerId}") // GET /api/rentals/history/customer/{customerId}
    public ResponseEntity<List<BookingDTO>> getCustomerHistory(@PathVariable Long customerId) {

        // Hämtar alla tidigare bokningar kopplade till en specifik kund.
        List<BookingDTO> dtoList = rentalService.getBookingHistoryForCustomer(customerId)
                .stream()
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());

        // Returnerar historiken som JSON-lista med 200 OK.
        return ResponseEntity.ok(dtoList);
    }
}
