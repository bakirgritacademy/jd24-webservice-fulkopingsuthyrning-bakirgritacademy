package se.fulkoping.rentalhub.config;
// Klassen ligger i paketet 'config' som innehåller konfigurationer för applikationen,
// till exempel säkerhet, Swagger och liknande.

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Denna klass konfigurerar Swagger (OpenAPI) för projektet.
 *
 * Swagger används för att automatiskt skapa en interaktiv dokumentation
 * av REST API:t – där utvecklare kan se alla endpoints, skicka testförfrågningar
 * och läsa beskrivningar direkt i webbläsaren.
 *
 * Klassen är kopplad till Spring Boot genom annoteringen @Configuration,
 * vilket gör att Spring automatiskt läser in den som en del av applikationens konfiguration.
 *
 * Swagger visas vanligtvis på adressen:
 *   http://localhost:8080/swagger-ui/index.html
 *
 * Klassen lägger även till stöd för autentisering via en API-nyckel
 * som skickas i HTTP-headern "X-API-KEY".
 */
@Configuration // Markerar att detta är en konfigurationsklass i Spring
public class SwaggerSetup {

    // @Bean betyder att metoden skapar ett objekt som hanteras av Spring.
    // I detta fall registreras ett OpenAPI-objekt som beskriver hela API:t.
    @Bean
    public OpenAPI rentalHubOpenAPI() {

        // Skapar en ny instans av OpenAPI – huvudobjektet för Swagger-konfigurationen.
        return new OpenAPI()

                // .info() används för att lägga till generell information om API:t.
                .info(new Info()
                        // Titel som visas högst upp i Swagger-gränssnittet.
                        .title("Fulköping RentalHub REST API")

                        // En kort beskrivning av vad API:t används till.
                        .description("""
                            Ett REST API för uthyrning av utrustning hos Fulköpings Uthyrnings AB.
                            Innehåller CRUD-funktioner, historik, validering, säkerhet och API-nyckelautentisering.
                            """)

                        // Versionsnummer för API:t, används för att hålla reda på förändringar.
                        .version("1.0.0")

                        // Kontaktinformation till ansvarig person eller support.
                        .contact(new Contact()
                                .name("Systemansvarig – Bakir")
                                .email("support@fulkoping-rentalhub.se"))

                        // Licensinformation – här används MIT-licensen som exempel.
                        .license(new License()
                                .name("Open License")
                                .url("https://opensource.org/licenses/MIT"))
                )

                // Här läggs ett säkerhetskrav till för hela API:t.
                // Det betyder att Swagger vet att en API-nyckel behövs för att anropa skyddade endpoints.
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))

                // .components() används för att definiera hur säkerheten ska fungera tekniskt.
                .components(new io.swagger.v3.oas.models.Components()

                        // Här definieras ett säkerhetsschema med namnet "ApiKeyAuth".
                        // Det talar om för Swagger att användaren måste ange en nyckel i HTTP-headern.
                        .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()

                                // Typen av säkerhet är en API-nyckel.
                                .type(Type.APIKEY)

                                // Platsen där nyckeln ska skickas – i HTTP-headern.
                                .in(In.HEADER)

                                // Namnet på headern där nyckeln ska stå.
                                .name("X-API-KEY")

                                // En kort beskrivning som visas i Swagger-gränssnittet.
                                .description("Ange din API-nyckel här (t.ex. fulkoping-secret-2025)")
                        )
                );
    }
}
