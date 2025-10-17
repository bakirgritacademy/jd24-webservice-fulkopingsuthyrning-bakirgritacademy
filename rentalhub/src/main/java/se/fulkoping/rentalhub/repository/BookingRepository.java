package se.fulkoping.rentalhub.repository;
// Klassen ligger i paketet 'repository', där man samlar alla interface
// som hanterar databasanrop. Spring Data JPA skapar automatiskt implementationer
// baserat på metodnamn, så man behöver sällan skriva SQL manuellt.

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.fulkoping.rentalhub.model.BookingEntity;

import java.util.List;

/**
 * Detta repository används för att hantera alla databasanrop som rör bokningar.
 *
 * Genom att ärva från JpaRepository<BookingEntity, Long> får vi automatiskt
 * färdiga metoder för att spara, hämta, uppdatera och ta bort bokningar.
 *
 * Här finns dessutom några anpassade metoder (query methods) som bygger
 * SQL-frågor automatiskt baserat på deras namn.
 */
@Repository // Markerar att detta är ett Spring-repository (hanteras av Spring automatiskt)
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    // ======================================================
    //          HÄMTA ALLA BOKNINGAR FÖR EN TILLGÅNG
    // ======================================================

    /**
     * Hämtar alla bokningar som tillhör en viss tillgång (asset)
     * och sorterar dem efter startdatum i fallande ordning.
     *
     * Spring tolkar metoden så här:
     *   SELECT * FROM rhub_bookings
     *   WHERE asset_id = :assetId
     *   ORDER BY start_date DESC;
     *
     * @param assetId tillgångens ID
     * @return lista med bokningar kopplade till tillgången
     */
    List<BookingEntity> findByAsset_IdOrderByStartDateDesc(Long assetId);

    // ======================================================
    //          HÄMTA ALLA BOKNINGAR FÖR EN KUND
    // ======================================================

    /**
     * Hämtar alla bokningar som tillhör en viss kund
     * och sorterar dem efter startdatum i fallande ordning.
     *
     * SQL-motsvarighet:
     *   SELECT * FROM rhub_bookings
     *   WHERE customer_id = :customerId
     *   ORDER BY start_date DESC;
     *
     * @param customerId kundens ID
     * @return lista med bokningar kopplade till kunden
     */
    List<BookingEntity> findByCustomer_IdOrderByStartDateDesc(Long customerId);

    // ======================================================
    //           KONTROLLERA OM NÅGON ÄR AKTIV
    // ======================================================

    /**
     * Kontrollerar om en viss tillgång redan har en aktiv bokning.
     *
     * Returnerar true om det finns en rad i databasen där:
     *   asset_id = :assetId och active = true
     *
     * SQL-motsvarighet:
     *   SELECT EXISTS(SELECT 1 FROM rhub_bookings
     *                 WHERE asset_id = :assetId AND active = true);
     */
    boolean existsByAsset_IdAndActiveTrue(Long assetId);

    /**
     * Kontrollerar om en viss kund har en pågående (aktiv) bokning.
     *
     * SQL-motsvarighet:
     *   SELECT EXISTS(SELECT 1 FROM rhub_bookings
     *                 WHERE customer_id = :customerId AND active = true);
     */
    boolean existsByCustomer_IdAndActiveTrue(Long customerId);
}
