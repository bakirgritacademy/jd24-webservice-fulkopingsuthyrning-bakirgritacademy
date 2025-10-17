package se.fulkoping.rentalhub.repository;
// Klassen ligger i paketet 'repository', där alla JPA-repositories finns.
// Repositories ansvarar för att prata direkt med databasen och hämta, spara,
// uppdatera eller ta bort data.

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.fulkoping.rentalhub.model.CustomerEntity;

/**
 * Detta repository används för att hantera alla databasanrop för kunder.
 *
 * Genom att ärva från JpaRepository<CustomerEntity, Long> får vi automatiskt
 * tillgång till alla vanliga CRUD-metoder (Create, Read, Update, Delete), t.ex.:
 *  - findAll()      → hämta alla kunder
 *  - findById(id)   → hämta en kund med visst ID
 *  - save(entity)   → spara eller uppdatera en kund
 *  - deleteById(id) → ta bort en kund
 *
 * Dessutom kan man lägga till egna metoder som skapar SQL-frågor automatiskt
 * baserat på metodnamn, som `existsByEmail` nedan.
 */
@Repository // Markerar interfacet som ett Repository-komponent (hanteras av Spring automatiskt)
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    /**
     * Kontrollerar om en kund redan finns registrerad med en viss e-postadress.
     *
     * Denna metod skapas automatiskt av Spring Data JPA eftersom den följer
     * namngivningsmönstret "existsBy<FältNamn>".
     *
     * SQL-motsvarighet:
     *   SELECT EXISTS(SELECT 1 FROM rhub_customers WHERE email = :email);
     *
     * @param email e-postadressen som ska kontrolleras
     * @return true om e-postadressen redan finns, annars false
     */
    boolean existsByEmail(String email);
}
