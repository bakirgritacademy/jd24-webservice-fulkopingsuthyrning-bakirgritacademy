package se.fulkoping.rentalhub.model;
// Klassen ligger i paketet 'model' som innehåller alla databasmodeller (entities).
// Dessa klasser motsvarar tabeller i databasen och används av Hibernate / JPA
// för att spara, läsa och uppdatera data automatiskt.

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Denna klass representerar en "tillgång" (utrustning eller objekt)
 * som kan hyras ut via systemet.
 *
 * Varje instans av denna klass motsvarar en rad i databastabellen `rhub_assets`.
 *
 * Klassen används i både backend-logik (t.ex. RentalCoordinatorService)
 * och när data skickas till/från API:et (ofta konverterad till AssetDTO).
 */
@Entity // Markerar att detta är en databasentitet (JPA kommer skapa en tabell för den)
@Table(name = "rhub_assets") // Anger tabellens namn i databasen
@Getter // Lombok: genererar automatiskt getters för alla fält
@Setter // Lombok: genererar automatiskt setters för alla fält
@NoArgsConstructor // Lombok: tom konstruktor (krävs av JPA)
@AllArgsConstructor // Lombok: konstruktor med alla fält som parametrar
@Builder // Lombok: gör det möjligt att skapa objekt med builder-mönster (t.ex. AssetEntity.builder().id(1L).build())
public class AssetEntity {

    // ======================================================
    //                PRIMÄRNYCKEL / ID
    // ======================================================

    @Id // Markerar detta fält som primärnyckel (unik identifierare)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Gör så att databasen själv genererar ID:t (auto-increment).
    private Long id;

    // ======================================================
    //                NAMN OCH KATEGORI
    // ======================================================

    @NotBlank(message = "Objektnamn krävs.")
    // Säkerställer att fältet inte är null eller tomt (endast blanksteg räknas som ogiltigt)
    @Size(min = 2, max = 100)
    // Sätter minsta och största tillåtna längd på namnet
    private String assetName; // Namn på tillgången, t.ex. "Borrmaskin Bosch"

    @NotBlank(message = "Kategori krävs.")
    // Måste anges, t.ex. "Elverktyg" eller "Maskiner"
    private String category;

    // ======================================================
    //                DAGSPRIS OCH TILLGÄNGLIGHET
    // ======================================================

    @NotNull
    // Fältet får inte vara null
    @Positive(message = "Pris måste vara större än noll.")
    // Säkerställer att värdet är ett positivt tal (> 0)
    private Double dailyRate; // Pris per dag för uthyrning

    @Column(nullable = false)
    // Kolumnen får inte vara null i databasen
    private boolean available = true;
    // Anger om tillgången är ledig (true) eller uthyrd (false)
    // Default-värdet sätts till true så att nya objekt är tillgängliga direkt.

    // ======================================================
    //                RELATION TILL BOKNINGAR
    // ======================================================

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    /**
     * En tillgång kan ha flera bokningar (one-to-many).
     *
     * - mappedBy = "asset": anger att motsvarande relation i BookingEntity heter "asset"
     * - cascade = CascadeType.ALL: ändringar på AssetEntity (t.ex. radering) påverkar även dess bokningar
     * - orphanRemoval = true: om en bokning tas bort från listan, tas den också bort från databasen
     * - fetch = FetchType.LAZY: bokningar hämtas bara från databasen när de faktiskt behövs
     */
    @JsonManagedReference
    /**
     * Undviker cirkulär referens i JSON (infinite recursion).
     * När AssetEntity omvandlas till JSON inkluderas dess bokningar,
     * men bokningarna innehåller inte tillbaka-länken till Asset igen.
     */
    private List<BookingEntity> bookingHistory = new ArrayList<>();
    // Lista med alla bokningar kopplade till denna tillgång (historik)
}
