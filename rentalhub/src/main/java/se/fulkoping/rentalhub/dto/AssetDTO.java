package se.fulkoping.rentalhub.dto;
// Klassen ligger i paketet 'dto' (Data Transfer Object).
// DTO-klasser används för att skicka data mellan backend och frontend (eller mellan olika lager i applikationen)
// utan att exponera hela databasmodellen (Entity-klasserna).

import lombok.*;
import java.util.List;

/**
 * Denna klass representerar en "tillgång" (t.ex. ett verktyg eller en maskin)
 * som ett förenklat objekt för överföring via API:et.
 *
 * Syftet med en DTO är att:
 *  - Minska mängden data som skickas över nätverket
 *  - Göra API-svaren lättare att läsa och använda
 *  - Skydda den interna databasmodellen (AssetEntity) från att exponeras direkt
 *
 * Denna klass används främst i controllers och service-lagret när man hämtar
 * eller skickar information om tillgångar till/från klienten.
 */
@Data // Lombok: genererar automatiskt getters, setters, equals(), hashCode() och toString()
@NoArgsConstructor // Lombok: skapar en tom (default) konstruktor
@AllArgsConstructor // Lombok: skapar en konstruktor med alla fält som parametrar
@Builder // Lombok: gör det möjligt att bygga objekt med "builder"-mönster (t.ex. AssetDTO.builder().id(1L).build())
public class AssetDTO {

    // Unikt ID för tillgången (samma som i databasen)
    private Long id;

    // Namn på tillgången, t.ex. "Borrmaskin Bosch"
    private String assetName;

    // Kategori eller typ av tillgång, t.ex. "Elverktyg" eller "Byggutrustning"
    private String category;

    // Kostnaden per dag för att hyra denna tillgång
    private Double dailyRate;

    // Anger om tillgången är tillgänglig att hyra (true) eller uthyrd (false)
    private boolean available;

    // En lista över tidigare bokningar för denna tillgång.
    // Detta fält är valfritt och används om man vill visa historik tillsammans med tillgången.
    private List<BookingDTO> bookingHistory;
}
