package se.fulkoping.rentalhub.model;
// Klassen ligger i paketet 'model' där alla databasmodeller (entities) finns.
// Denna modell motsvarar tabellen för bokningar i databasen (t.ex. uthyrningar).

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Denna klass representerar en bokning (uthyrning) av en tillgång
 * som en viss kund har gjort i systemet.
 *
 * Varje rad i databastabellen `rhub_bookings` motsvarar en specifik bokning,
 * med startdatum, slutdatum och information om vilken kund och tillgång den gäller.
 */
@Entity // Markerar klassen som en JPA-entity (databastabell)
@Table(name = "rhub_bookings") // Anger tabellens namn i databasen
@Getter // Lombok: genererar getters automatiskt
@Setter // Lombok: genererar setters automatiskt
@NoArgsConstructor // Lombok: tom konstruktor (krävs av JPA)
@AllArgsConstructor // Lombok: konstruktor med alla fält
@Builder // Lombok: gör det möjligt att skapa objekt med builder-mönster
public class BookingEntity {

    // ======================================================
    //                  PRIMÄRNYCKEL / ID
    // ======================================================

    @Id // Markerar detta fält som primärnyckel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Databasen skapar ID:t automatiskt (auto-increment)
    private Long id;

    // ======================================================
    //                 RELATION TILL ASSET
    // ======================================================

    @ManyToOne(optional = false)
    // Flera bokningar kan kopplas till samma tillgång (ManyToOne).
    // "optional = false" betyder att varje bokning måste tillhöra en tillgång (kan inte vara null).
    @JoinColumn(name = "asset_id")
    // Anger att kolumnen i databasen ska heta "asset_id" (foreign key till rhub_assets).
    @JsonBackReference
    /**
     * @JsonBackReference används tillsammans med @JsonManagedReference i AssetEntity.
     * Det förhindrar oändliga loopar när objekt serialiseras till JSON.
     *
     * Det betyder: när en tillgång (AssetEntity) omvandlas till JSON visas dess bokningar,
     * men i varje bokning visas inte tillgången igen — för att undvika cirkulär referens.
     */
    private AssetEntity asset;

    // ======================================================
    //                 RELATION TILL CUSTOMER
    // ======================================================

    @ManyToOne(optional = false)
    // Flera bokningar kan kopplas till samma kund (ManyToOne).
    // "optional = false" betyder att varje bokning måste ha en kund.
    @JoinColumn(name = "customer_id")
    // Anger kolumnnamnet i databasen som kopplar till kundens ID (foreign key till rhub_customers).
    private CustomerEntity customer;

    // ======================================================
    //                 BOKNINGSINFORMATION
    // ======================================================

    private LocalDate startDate; // Datum då uthyrningen börjar
    private LocalDate endDate;   // Datum då uthyrningen slutar

    private boolean active = true;
    // Anger om bokningen fortfarande är aktiv (true) eller avslutad (false).
    // Som standard är alla nya bokningar aktiva.

    private String note;
    // Valfri anteckning om bokningen (t.ex. “Skadad laddare vid återlämning”).
}
