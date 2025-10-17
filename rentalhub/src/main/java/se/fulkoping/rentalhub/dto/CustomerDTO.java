package se.fulkoping.rentalhub.dto;
// Klassen ligger i paketet 'dto' (Data Transfer Object).
// DTO används för att skicka och ta emot data mellan backend (servern) och frontend (klienten)
// på ett förenklat sätt utan att exponera hela databasmodellen (CustomerEntity).

import lombok.*;
import java.util.List;

/**
 * Denna klass representerar en kund i systemet som ett dataöverföringsobjekt (DTO).
 *
 * Syftet med en DTO (Data Transfer Object) är att:
 *  - Skicka endast relevant data via API:et
 *  - Göra svaret enklare och tydligare för frontend
 *  - Skydda den interna databasmodellen (CustomerEntity)
 *
 * Klassen används t.ex. i CustomerController för att hämta, skapa och uppdatera kundinformation.
 */
@Data // Lombok: genererar getters, setters, equals(), hashCode() och toString() automatiskt
@NoArgsConstructor // Lombok: skapar en tom konstruktor utan argument
@AllArgsConstructor // Lombok: skapar en konstruktor som tar alla fält som parametrar
@Builder // Lombok: gör det möjligt att skapa objekt med "builder"-mönster (t.ex. CustomerDTO.builder().id(1L).build())
public class CustomerDTO {

    // Unikt ID för kunden (primärnyckeln i databasen)
    private Long id;

    // Kundens förnamn
    private String firstName;

    // Kundens efternamn
    private String lastName;

    // Kundens e-postadress, används för kontakt och identifiering
    private String email;

    // Kundens telefonnummer
    private String phone;

    // En lista över kundens bokningar (valfritt fält).
    // Denna används om man vill inkludera bokningshistorik direkt i kundens data.
    private List<BookingDTO> bookings;
}
