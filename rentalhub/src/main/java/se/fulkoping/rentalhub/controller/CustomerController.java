package se.fulkoping.rentalhub.controller;
// Denna klass ligger i paketet 'controller' som innehåller alla REST-kontrollers i projektet.
// Controllers tar emot HTTP-anrop från frontend eller externa system och skickar vidare logiken
// till service-lagret (som i sin tur pratar med databasen).

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.fulkoping.rentalhub.dto.CustomerDTO;
import se.fulkoping.rentalhub.model.CustomerEntity;
import se.fulkoping.rentalhub.model.EntityMapper;
import se.fulkoping.rentalhub.service.RentalCoordinatorService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Denna controller ansvarar för all hantering av kunder i systemet.
 *
 * Den erbjuder API-endpoints för CRUD-operationer (Create, Read, Update, Delete)
 * så att man kan lägga till, hämta, ändra och ta bort kunder i databasen.
 *
 * Klassen samarbetar med:
 * - `RentalCoordinatorService` (som innehåller affärslogiken)
 * - `EntityMapper` (som omvandlar mellan CustomerEntity och CustomerDTO)
 *
 * Resultatet skickas tillbaka som JSON.
 */
@RestController // Markerar att detta är en REST-controller (returnerar JSON i svaren)
@RequestMapping("/api/customers") // Bas-URL för alla endpoints i denna klass
@RequiredArgsConstructor // Skapar automatiskt en konstruktor för alla 'final' fält (via Lombok)
@Tag(name = "Customers", description = "API för att hantera kunder")
// @Tag används av Swagger för att gruppera endpoints under fliken “Customers”
public class CustomerController {

    // Dessa fält injiceras automatiskt av Spring genom konstruktorn (tack vare @RequiredArgsConstructor)
    private final RentalCoordinatorService rentalService; // Service-lager som hanterar logiken för kunder
    private final EntityMapper mapper; // Mapper som konverterar mellan entiteter och DTO-objekt

    // ======================================================
    //                   HÄMTA ALLA KUNDER
    // ======================================================

    @GetMapping // Anropas när klienten gör ett GET-anrop till /api/customers
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {

        // Hämtar alla kunder från databasen via service-lagret.
        // listAllCustomers() returnerar en lista av CustomerEntity-objekt.
        List<CustomerDTO> dtoList = rentalService.listAllCustomers()
                .stream() // Gör listan strömbaserad (Stream API)
                .map(mapper::toCustomerDTO) // Omvandlar varje CustomerEntity till CustomerDTO
                .collect(Collectors.toList()); // Samlar tillbaka till en vanlig lista

        // Skickar listan till klienten tillsammans med HTTP-status 200 OK.
        return ResponseEntity.ok(dtoList);
    }

    // ======================================================
    //                     SKAPA KUND
    // ======================================================

    @PostMapping // Anropas när klienten skickar ett POST-anrop till /api/customers
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerEntity customer) {
        // @RequestBody gör att JSON-datan i HTTP-anropet automatiskt mappas till ett CustomerEntity-objekt.
        // @Valid säkerställer att alla fält i CustomerEntity uppfyller de valideringsregler som satts med annoteringar.

        try {
            // Skickar den nya kunden till service-lagret för att sparas i databasen.
            CustomerEntity saved = rentalService.addCustomer(customer);

            // Om allt går bra returneras HTTP-status 201 (Created)
            // samt den sparade kunden som DTO.
            return ResponseEntity.status(201).body(mapper.toCustomerDTO(saved));

        } catch (Exception e) {
            // Om något går fel (t.ex. validering eller databasfel) returneras HTTP 400 (Bad Request)
            // tillsammans med felmeddelandet.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    //                    UPPDATERA KUND
    // ======================================================

    @PutMapping("/{id}") // Anropas när klienten skickar PUT /api/customers/{id}
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerEntity updatedCustomer) {
        // @PathVariable kopplar värdet i URL:en till variabeln 'id'.
        // @RequestBody innehåller den nya datan som ska uppdateras.
        // @Valid säkerställer att alla valideringsregler uppfylls.

        try {
            // Uppdaterar kundens data via service-lagret.
            CustomerEntity saved = rentalService.updateCustomer(id, updatedCustomer);

            // Returnerar den uppdaterade kunden som DTO med HTTP-status 200 (OK).
            return ResponseEntity.ok(mapper.toCustomerDTO(saved));

        } catch (Exception e) {
            // Vid fel (t.ex. om kunden inte finns) returneras HTTP 400 (Bad Request)
            // med felmeddelandet som text.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    //                   TA BORT KUND
    // ======================================================

    @DeleteMapping("/{id}") // Anropas när klienten skickar DELETE /api/customers/{id}
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        // Tar bort en kund med ett visst ID.
        // I systemet finns en regel som säger att en kund inte får tas bort
        // om den har en aktiv uthyrning/bokning.

        try {
            // Försöker ta bort kunden via service-lagret.
            rentalService.deleteCustomer(id);

            // Om det går bra skickas HTTP-status 204 (No Content),
            // vilket betyder att borttagningen lyckades utan något innehåll i svaret.
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            // Om borttagningen inte är tillåten (t.ex. på grund av aktiv bokning)
            // skickas HTTP 409 (Conflict) med ett förklarande meddelande.
            return ResponseEntity.status(409).body(e.getMessage());

        } catch (Exception e) {
            // Vid andra typer av fel skickas HTTP 400 (Bad Request).
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
