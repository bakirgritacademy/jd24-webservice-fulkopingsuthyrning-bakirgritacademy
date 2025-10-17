package se.fulkoping.rentalhub.service;
// Klassen ligger i paketet 'service', där all affärslogik finns.
// Controllers anropar metoder i service-lagret för att hantera data,
// och service-lagret använder repositories för att kommunicera med databasen.

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.fulkoping.rentalhub.dto.BookingDTO;
import se.fulkoping.rentalhub.model.AssetEntity;
import se.fulkoping.rentalhub.model.BookingEntity;
import se.fulkoping.rentalhub.model.CustomerEntity;
import se.fulkoping.rentalhub.repository.AssetRepository;
import se.fulkoping.rentalhub.repository.BookingRepository;
import se.fulkoping.rentalhub.repository.CustomerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Denna service-klass fungerar som "hjärnan" i systemet.
 * Den samordnar logiken mellan kunder, tillgångar och bokningar.
 *
 * Här finns metoder för:
 *  - Att skapa, uppdatera och ta bort tillgångar (Assets)
 *  - Att hantera kunder (Customers)
 *  - Att skapa, avsluta och visa bokningar (Bookings)
 *
 * Alla controllers (API-klasser) anropar metoder i denna klass
 * istället för att direkt arbeta mot databasen.
 */
@Service // Markerar denna klass som en Spring Service-komponent
@RequiredArgsConstructor // Lombok: genererar en konstruktor som injicerar alla 'final'-fält
@Slf4j // Lombok: skapar en logginstans (log.info(), log.warn() etc.)
public class RentalCoordinatorService {

    // ======================================================
    //        REPOSITORIES FÖR DATABASKOMMUNIKATION
    // ======================================================
    private final AssetRepository assetRepo;       // Hanterar utrustning (tillgångar)
    private final CustomerRepository customerRepo; // Hanterar kunddata
    private final BookingRepository bookingRepo;   // Hanterar bokningar

    // ======================================================
    // 1️⃣   HANTERING AV TILLGÅNGAR (ASSETS)
    // ======================================================

    public List<AssetEntity> listAllAssets() {
        // Hämtar alla tillgångar i databasen
        return assetRepo.findAll();
    }

    public List<AssetEntity> listAvailableAssets() {
        // Hämtar endast de tillgångar som är markerade som tillgängliga
        return assetRepo.findByAvailableTrue();
    }

    public AssetEntity addAsset(AssetEntity asset) {
        // Sparar en ny tillgång i databasen
        return assetRepo.save(asset);
    }

    public AssetEntity updateAsset(Long id, AssetEntity updated) {
        // Hämtar den tillgång som ska uppdateras
        AssetEntity existing = assetRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tillgång med id " + id + " finns ej"));

        // Uppdaterar fälten med nya värden
        existing.setAssetName(updated.getAssetName());
        existing.setCategory(updated.getCategory());
        existing.setDailyRate(updated.getDailyRate());
        existing.setAvailable(updated.isAvailable());

        // Sparar uppdaterad tillgång
        return assetRepo.save(existing);
    }

    public void deleteAsset(Long id) {
        // Kontroll: får inte ta bort en tillgång som har aktiv bokning
        if (bookingRepo.existsByAsset_IdAndActiveTrue(id)) {
            throw new IllegalStateException("Kan inte ta bort tillgång – aktiv bokning finns.");
        }
        assetRepo.deleteById(id);
    }

    // ======================================================
    // 2️⃣   HANTERING AV KUNDER (CUSTOMERS)
    // ======================================================

    public List<CustomerEntity> listAllCustomers() {
        // Hämtar alla kunder från databasen
        return customerRepo.findAll();
    }

    public CustomerEntity addCustomer(CustomerEntity c) {
        // Validerar att e-post finns och inte är tom
        if (c.getEmail() == null || c.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-post krävs");
        }

        // Kontroll: får inte registrera kund med e-post som redan används
        if (customerRepo.existsByEmail(c.getEmail())) {
            throw new IllegalStateException("E-post används redan");
        }

        // Sparar kunden i databasen
        return customerRepo.save(c);
    }

    public CustomerEntity updateCustomer(Long id, CustomerEntity updated) {
        // Hämtar befintlig kund, annars kastas NoSuchElementException
        CustomerEntity existing = customerRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Kund med id " + id + " finns ej"));

        // Uppdaterar kundens fält
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());

        // Sparar uppdateringen
        return customerRepo.save(existing);
    }

    public void deleteCustomer(Long id) {
        // Kontroll: kund med aktiv bokning får inte tas bort
        if (bookingRepo.existsByCustomer_IdAndActiveTrue(id)) {
            throw new IllegalStateException("Kan inte ta bort kund – aktiv bokning finns.");
        }

        // Tar bort kund från databasen
        customerRepo.deleteById(id);
    }

    // ======================================================
    // 3️⃣   HANTERING AV BOKNINGAR (RENTALS / BOOKINGS)
    // ======================================================

    public List<BookingEntity> listAllBookings() {
        // Hämtar alla bokningar (aktiva + historiska)
        return bookingRepo.findAll();
    }

    public BookingEntity getBookingById(Long id) {
        // Hämtar en specifik bokning via dess ID
        return bookingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bokning med id " + id + " finns ej"));
    }

    // -------------------------------
    //   SKAPA NY BOKNING
    // -------------------------------
    @Transactional // Säkerställer att hela metoden körs som en transaktion (alla ändringar sparas eller rullas tillbaka)
    public BookingEntity registerBooking(Long assetId, Long customerId, BookingDTO req) {
        // Hämtar tillgång från databasen
        AssetEntity asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new NoSuchElementException("Tillgång med id " + assetId + " finns ej"));

        // Kontrollera att tillgången inte redan är uthyrd
        if (!asset.isAvailable()) {
            throw new IllegalStateException("Tillgången är redan uthyrd");
        }

        // Hämtar kund från databasen
        CustomerEntity customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Kund med id " + customerId + " finns ej"));

        // Kontrollera att det inte finns aktiv bokning på tillgången
        if (bookingRepo.existsByAsset_IdAndActiveTrue(assetId)) {
            throw new IllegalStateException("Det finns redan en aktiv bokning på denna tillgång");
        }

        // Om startdatum inte skickas med, sätt dagens datum
        LocalDate start = (req != null && req.getStartDate() != null)
                ? req.getStartDate()
                : LocalDate.now();

        // Skapar nytt BookingEntity-objekt
        BookingEntity booking = BookingEntity.builder()
                .asset(asset)
                .customer(customer)
                .startDate(start)
                .endDate(null)
                .active(true)
                .note(req != null ? req.getNote() : null)
                .build();

        // Markera tillgång som uthyrd
        asset.setAvailable(false);
        assetRepo.save(asset);

        // Spara bokningen i databasen
        BookingEntity saved = bookingRepo.save(booking);

        // Logga till konsolen för spårbarhet
        log.info("🟢 Bokning skapad: bookingId={}, assetId={}, customerId={}",
                saved.getId(), asset.getId(), customer.getId());

        return saved;
    }

    // -------------------------------
    //   AVSLUTA BOKNING
    // -------------------------------
    @Transactional
    public BookingEntity closeBooking(Long bookingId) {
        // Hämtar bokningen
        BookingEntity booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Bokning med id " + bookingId + " finns ej"));

        // Kontrollera att den inte redan är avslutad
        if (!booking.isActive()) {
            throw new IllegalStateException("Bokningen är redan avslutad");
        }

        // Sätt bokningen som avslutad och registrera dagens datum som slutdatum
        booking.setActive(false);
        booking.setEndDate(LocalDate.now());

        // Markera tillgången som tillgänglig igen
        AssetEntity asset = booking.getAsset();
        asset.setAvailable(true);
        assetRepo.save(asset);

        // Spara bokningsändringen
        BookingEntity saved = bookingRepo.save(booking);

        // Logga händelsen
        log.info("🔵 Bokning avslutad: bookingId={}, assetId={}", saved.getId(), asset.getId());

        return saved;
    }

    // -------------------------------
    //   TA BORT BOKNING
    // -------------------------------
    public void deleteBooking(Long id) {
        // Hämtar bokningen
        BookingEntity booking = bookingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bokning med id " + id + " finns ej"));

        // Kontroll: aktiv bokning får inte tas bort
        if (booking.isActive()) {
            throw new IllegalStateException("Kan inte ta bort aktiv bokning. Avsluta först.");
        }

        // Tar bort bokningen från databasen
        bookingRepo.deleteById(id);
    }

    // -------------------------------
    //   HÄMTA HISTORIK
    // -------------------------------
    public List<BookingEntity> getBookingHistoryForAsset(Long assetId) {
        // Hämtar alla bokningar kopplade till en viss tillgång
        return bookingRepo.findByAsset_IdOrderByStartDateDesc(assetId);
    }

    public List<BookingEntity> getBookingHistoryForCustomer(Long customerId) {
        // Hämtar alla bokningar kopplade till en viss kund
        return bookingRepo.findByCustomer_IdOrderByStartDateDesc(customerId);
    }
}
