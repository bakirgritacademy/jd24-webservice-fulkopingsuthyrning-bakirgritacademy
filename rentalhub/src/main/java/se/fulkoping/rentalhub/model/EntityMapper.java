package se.fulkoping.rentalhub.model;
// Klassen ligger i paketet 'model', men fungerar egentligen som en "hjälpklass" (mapper)
// som omvandlar mellan Entity-objekt (databasmodeller) och DTO-objekt (data som skickas till/från API:et).

import org.springframework.stereotype.Component;
import se.fulkoping.rentalhub.model.*;
import se.fulkoping.rentalhub.dto.*;
import java.util.stream.Collectors;

/**
 * Denna klass används för att omvandla mellan olika objekt:
 *  - Från Entity (som används i databasen) till DTO (som används i API-svar)
 *
 * Syftet med en mapper är att separera den interna databasmodellen
 * från de objekt som skickas till frontend, så att man:
 *  1. Undviker att råa databasobjekt exponeras i API:et
 *  2. Får tydligare struktur i JSON-svaren
 *  3. Kan kontrollera exakt vilken information som skickas ut
 */
@Component // Gör klassen till en Spring-komponent som kan användas (autowired) i controllers och services
public class EntityMapper {

    // ======================================================
    //                 KONVERTERA ASSET → DTO
    // ======================================================

    public AssetDTO toAssetDTO(AssetEntity entity) {
        // Om entity är null (t.ex. ej hittad i databasen), returnera null direkt
        if (entity == null) return null;

        // Skapar en AssetDTO från en AssetEntity med hjälp av builder-mönstret
        return AssetDTO.builder()
                .id(entity.getId())                     // Tillgångens ID
                .assetName(entity.getAssetName())       // Namn
                .category(entity.getCategory())         // Kategori
                .dailyRate(entity.getDailyRate())       // Dagspris
                .available(entity.isAvailable())        // Tillgänglighetsstatus

                // Konvertera listan med bokningar om den finns
                .bookingHistory(entity.getBookingHistory() == null ? null :
                        entity.getBookingHistory().stream()
                                .map(this::toBookingDTO) // För varje bokning: skapa en BookingDTO
                                .collect(Collectors.toList()))
                .build(); // Skapar färdigt DTO-objektet
    }

    // ======================================================
    //                KONVERTERA BOOKING → DTO
    // ======================================================

    public BookingDTO toBookingDTO(BookingEntity entity) {
        if (entity == null) return null;

        return BookingDTO.builder()
                .id(entity.getId())                       // Bokningens ID
                .startDate(entity.getStartDate())         // Startdatum
                .endDate(entity.getEndDate())             // Slutdatum
                .active(entity.isActive())                // Aktiv status (true = pågående)
                .note(entity.getNote())                   // Eventuell anteckning

                // Nedanstående fält hämtas från kopplingar till andra entities
                .assetId(entity.getAsset() != null ? entity.getAsset().getId() : null)
                .assetName(entity.getAsset() != null ? entity.getAsset().getAssetName() : null)

                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .customerName(entity.getCustomer() != null ?
                        entity.getCustomer().getFirstName() + " " + entity.getCustomer().getLastName() : null)
                .build();
    }

    // ======================================================
    //              KONVERTERA CUSTOMER → DTO
    // ======================================================

    public CustomerDTO toCustomerDTO(CustomerEntity entity) {
        if (entity == null) return null;

        return CustomerDTO.builder()
                .id(entity.getId())                   // Kundens ID
                .firstName(entity.getFirstName())     // Förnamn
                .lastName(entity.getLastName())       // Efternamn
                .email(entity.getEmail())             // E-postadress
                .phone(entity.getPhone())             // Telefonnummer

                // Konvertera kundens bokningar om de finns
                .bookings(entity.getBookings() == null ? null :
                        entity.getBookings().stream()
                                .map(this::toBookingDTO) // Gör om varje bokning till BookingDTO
                                .collect(Collectors.toList()))
                .build();
    }
}
