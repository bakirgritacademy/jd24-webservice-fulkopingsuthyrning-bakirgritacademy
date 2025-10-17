package se.fulkoping.rentalhub.repository;
// Klassen ligger i paketet 'repository', som innehåller alla klasser och interface
// för kommunikation med databasen. Här används Spring Data JPA för att automatiskt
// skapa standardfrågor (CRUD) utan att skriva SQL själv.

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.fulkoping.rentalhub.model.AssetEntity;
import java.util.List;

/**
 * Detta repository hanterar databasanrop för entiteten AssetEntity.
 *
 * Genom att ärva från JpaRepository får man automatiskt tillgång till
 * färdiga metoder för vanliga operationer som:
 *  - findAll()      → hämta alla poster
 *  - findById(id)   → hämta en post via ID
 *  - save(entity)   → spara eller uppdatera en post
 *  - deleteById(id) → ta bort en post
 *
 * Man kan dessutom definiera egna metoder (s.k. "query methods")
 * genom att följa Spring Data JPA:s namngivningsregler.
 */
@Repository // Markerar detta interface som ett Repository-komponent (för databasanrop)
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {
    /**
     * Hittar alla tillgångar som är tillgängliga att hyra.
     *
     * Tack vare Spring Data JPA behövs ingen SQL — metoden skapas automatiskt
     * eftersom namnet följer konventionen "findBy<fält>True()".
     *
     * Denna metod motsvarar SQL:
     * SELECT * FROM rhub_assets WHERE available = true;
     */
    List<AssetEntity> findByAvailableTrue();
}
