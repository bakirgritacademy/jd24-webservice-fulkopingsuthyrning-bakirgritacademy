package se.fulkoping.rentalhub.filter;
// Klassen ligger i paketet 'filter' och används för att filtrera inkommande HTTP-anrop
// innan de når controllers. Här används filtret för att kontrollera att rätt API-nyckel skickas med.

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Detta filter används för enkel API-nyckelautentisering.
 *
 * Funktion:
 *  - Tillåter alla GET-anrop och Swagger-dokumentation utan nyckel.
 *  - Kräver giltig API-nyckel i headern "X-API-KEY" för POST, PUT och DELETE-anrop.
 *  - Om nyckeln saknas eller är fel, skickas ett JSON-fel tillbaka (samma format som GlobalExceptionHandler).
 *
 * Filtret körs en gång per inkommande förfrågan (därav arv från OncePerRequestFilter).
 */
@Slf4j // Lombok: skapar en logginstans (log) så att man kan logga med log.info(), log.warn() osv.
@Component // Markerar att denna klass är en Spring-komponent och ska aktiveras automatiskt
public class ApiKeyFilter extends OncePerRequestFilter {

    // Namnet på HTTP-headern som förväntas innehålla API-nyckeln.
    private static final String HEADER = "X-API-KEY";

    @Value("${app.api.key}")
    // Hämtar den giltiga API-nyckeln från application.properties (t.ex. app.api.key=fulkoping-secret-2025)
    private String validApiKey;

    /**
     * Huvudmetoden i filtret. Den körs automatiskt före alla kontroller i applikationen.
     * Här avgörs om anropet får fortsätta (om nyckeln är giltig) eller blockeras.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Hämtar grundläggande information om inkommande förfrågan.
        String method = request.getMethod();         // HTTP-metoden (t.ex. GET, POST)
        String uri = request.getRequestURI();        // Vilken endpoint som anropas
        String apiKey = request.getHeader(HEADER);   // Hämtar API-nyckeln från headern

        // Loggar inkommande anrop till konsolen, exempel:
        // ➡️ Request: POST /api/assets från 192.168.1.10
        log.info("➡️ Request: {} {} från {}", method, uri, request.getRemoteAddr());

        // Tillåter GET-anrop och Swagger utan krav på API-nyckel
        // Detta gör att dokumentation och vanliga hämtningar fungerar utan autentisering.
        if (method.equalsIgnoreCase("GET") || uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response); // Skickar vidare till nästa filter eller controller
            return; // Avslutar metoden här
        }

        // Kontroll av API-nyckel för alla andra metoder (POST, PUT, DELETE)
        if (apiKey == null || !apiKey.equals(validApiKey)) {
            // Loggar att nyckeln är ogiltig eller saknas
            log.warn("❌ Ogiltig API-nyckel för {} {}", method, uri);

            // Skapar ett JSON-svar som informerar klienten om att autentiseringen misslyckades
            response.setStatus(HttpStatus.UNAUTHORIZED.value());                    // HTTP 401 Unauthorized
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());           // Säkerställer rätt teckenkodning
            response.setContentType("application/json; charset=UTF-8");             // Anger att svaret är JSON i UTF-8

            // Bygger JSON-texten som returneras till klienten
            String json = String.format("""
                {
                  "timestamp": "%s",
                  "status": %d,
                  "error": "%s",
                  "message": "API-nyckel saknas eller är ogiltig",
                  "path": "%s"
                }
                """,
                    LocalDateTime.now(),                        // Tidpunkt då felet inträffade
                    HttpStatus.UNAUTHORIZED.value(),             // HTTP-statuskod (401)
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),   // Text: "Unauthorized"
                    uri                                          // Sökvägen som orsakade felet
            );

            // Skriver ut JSON-svaret till klienten och avslutar här (ingen åtkomst ges)
            response.getWriter().write(json);
            return;
        }

        // Om API-nyckeln är giltig:
        // Skapar en enkel "autentisering" i Spring Securitys SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("apiKeyUser", null, Collections.emptyList());

        // Sätter denna autentisering i det aktuella säkerhetskontextet
        // så att resten av applikationen vet att anropet är godkänt.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Loggar att allt gick bra
        log.info("✅ API-nyckel godkänd för {}", uri);

        // Skickar vidare till nästa filter eller till controller-metoden
        filterChain.doFilter(request, response);
    }
}
