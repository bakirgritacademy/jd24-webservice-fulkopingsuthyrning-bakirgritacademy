package se.fulkoping.rentalhub.dto;
// Klassen ligger i paketet 'dto' (Data Transfer Object).
// DTO används för att överföra data mellan backend (servern) och frontend (klienten)
// utan att exponera hela databasmodellen (BookingEntity).

import lombok.*;
import java.time.LocalDate;

/**
 * Denna klass representerar en bokning (hyra av utrustning) som ett dataobjekt
 * som skickas till eller från API:et.
 *
 * DTO (Data Transfer Object) används här för att:
 *  - Skicka endast den nödvändiga informationen till klienten
 *  - Göra API-svaren enklare och renare
 *  - Undvika att databasens hela "BookingEntity" exponeras utåt
 *
 * Klassen används t.ex. i RentalController för att visa och ta emot bokningsinformation.
 */
@Data // Lombok: genererar automatiskt getters, setters, equals(), hashCode() och toString()
@NoArgsConstructor // Lombok: skapar en tom konstruktor (utan argument)
@AllArgsConstructor // Lombok: skapar en konstruktor som tar alla fält som parametrar
@Builder // Lombok: gör det möjligt att skapa objekt med "builder"-mönster (t.ex. BookingDTO.builder().id(1L).build())
public class BookingDTO {

    // Unikt ID för bokningen (primärnyckeln i databasen)
    private Long id;

    // Startdatum för hyran – när kunden börjar hyra tillgången
    private LocalDate startDate;

    // Slutdatum för hyran – när tillgången ska lämnas tillbaka
    private LocalDate endDate;

    // Anger om bokningen fortfarande är aktiv (true) eller avslutad (false)
    private boolean active;

    // En valfri anteckning som kunden eller personalen kan lägga till om bokningen
    private String note;

    // ID för tillgången som bokningen gäller (koppling till AssetEntity)
    private Long assetId;

    // Namnet på tillgången, används för att göra det enklare att visa i t.ex. UI eller historik
    private String assetName;

    // ID för kunden som gjort bokningen (koppling till CustomerEntity)
    private Long customerId;

    // Kundens namn, så att man kan visa vem som hyrt utan att behöva hämta hela kundobjektet
    private String customerName;
}
