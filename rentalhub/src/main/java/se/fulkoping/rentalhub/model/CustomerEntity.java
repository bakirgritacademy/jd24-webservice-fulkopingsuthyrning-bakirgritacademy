package se.fulkoping.rentalhub.model;
// Klassen ligger i paketet 'model', där alla JPA-entity-klasser (databasmodeller) finns.
// En entity motsvarar en tabell i databasen och används för att spara och läsa data via JPA/Hibernate.

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Denna klass representerar en kund i uthyrningssystemet.
 *
 * Varje instans motsvarar en rad i databastabellen `rhub_customers`.
 * Kunden kan ha flera bokningar kopplade till sig.
 *
 * Klassen används för att hantera kunddata i backend,
 * men för kommunikation med frontend används oftast en DTO-version (CustomerDTO).
 */
@Entity // Markerar att klassen är en databasentitet som hanteras av JPA
@Table(name = "rhub_customers") // Anger tabellens namn i databasen
@Getter // Lombok: skapar automatiskt getters för alla fält
@Setter // Lombok: skapar automatiskt setters för alla fält
@NoArgsConstructor // Lombok: skapar en tom konstruktor (krävs av JPA)
@AllArgsConstructor // Lombok: skapar en konstruktor som tar alla fält
@Builder // Lombok: gör det möjligt att skapa objekt med builder-mönster
public class CustomerEntity {

    // ======================================================
    //                  PRIMÄRNYCKEL / ID
    // ======================================================

    @Id // Markerar detta fält som primärnyckel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Gör att databasen själv genererar värdet (auto-increment)
    private Long id;

    // ======================================================
    //                  PERSONUPPGIFTER
    // ======================================================

    @NotBlank(message = "Förnamn krävs.")
    // Säkerställer att förnamnet inte är tomt eller bara mellanslag
    private String firstName;

    @NotBlank(message = "Efternamn krävs.")
    // Säkerställer att efternamnet inte är tomt
    private String lastName;

    @Email // Validerar att fältet innehåller en giltig e-postadress
    @NotBlank(message = "E-post krävs.")
    @Column(unique = true)
    // Anger att e-postadressen måste vara unik i databasen – inga dubbletter tillåtna
    private String email;

    // Telefonnummer är frivilligt (inga valideringsregler)
    private String phone;

    // ======================================================
    //                  RELATION TILL BOKNINGAR
    // ======================================================

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    /**
     * En kund kan ha flera bokningar (One-to-Many-relation).
     *
     * - mappedBy = "customer" betyder att motsvarande fält i BookingEntity heter "customer"
     * - cascade = CascadeType.ALL gör att ändringar på kund (t.ex. radering)
     *   påverkar kundens bokningar automatiskt.
     * - orphanRemoval = true betyder att om man tar bort en bokning från listan här
     *   så tas den bort helt ur databasen.
     */
    private List<BookingEntity> bookings = new ArrayList<>();
    // Innehåller alla bokningar som denna kund har gjort.
    // När kunden laddas från databasen kan man direkt se dess historik.
}
