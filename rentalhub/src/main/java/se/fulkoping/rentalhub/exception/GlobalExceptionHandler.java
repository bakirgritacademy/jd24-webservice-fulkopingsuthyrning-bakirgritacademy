package se.fulkoping.rentalhub.exception;
// Klassen ligger i paketet 'exception' där man samlar applikationens felhantering.
// Den ansvarar för att fånga upp undantag (exceptions) och skicka
// ett konsekvent JSON-svar tillbaka till klienten (frontend, API-användare).

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Denna klass hanterar alla undantag (fel) som kastas i applikationen på ett centralt ställe.
 *
 * Den ser till att oavsett var felet uppstår i koden, så får klienten ett
 * enhetligt JSON-svar med information om felet.
 *
 * Det gör att API:et blir mer professionellt och förutsägbart.
 *
 * Exempel på JSON-svar som skickas:
 * {
 *   "timestamp": "2025-10-17T12:00:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Felmeddelande",
 *   "path": "/api/customers"
 * }
 */
@ControllerAdvice // Gör att denna klass "övervakar" alla controllers i projektet och fångar deras fel
public class GlobalExceptionHandler {

    // ======================================================
    //      400 – Hantering av valideringsfel (@Valid)
    // ======================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Denna metod körs när ett valideringsfel inträffar, t.ex. om @Valid i en controller misslyckas.
    // Det händer när data från klienten inte uppfyller reglerna (t.ex. tomt namn eller ogiltig e-post).
    public ResponseEntity<Map<String, Object>> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest req) {

        // Skapar två mappar: en för svarets huvuddata och en för detaljerade fältfel.
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        // Loopar igenom alla valideringsfel och sparar fältnamn + felmeddelande i "errors".
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        // Bygger upp själva JSON-svaret.
        body.put("timestamp", LocalDateTime.now());                // När felet inträffade
        body.put("status", HttpStatus.BAD_REQUEST.value());        // HTTP-statuskod (400)
        body.put("error", "Validation failed");                    // Kort beskrivning
        body.put("message", "Fel i indata");                       // Allmänt meddelande
        body.put("details", errors);                               // Alla fältfel i form av nyckel/värde
        body.put("path", req.getRequestURI());                     // Vilken endpoint som orsakade felet

        // Returnerar svaret med rätt HTTP-status och innehållstyp
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(body);
    }

    // ======================================================
    //      400 – Ogiltigt anrop (IllegalArgumentException)
    // ======================================================

    @ExceptionHandler(IllegalArgumentException.class)
    // Hanterar fall där metoden fått ogiltiga argument, t.ex. felaktigt ID.
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // ======================================================
    //      409 – Konflikt (t.ex. otillåten borttagning)
    // ======================================================

    @ExceptionHandler(IllegalStateException.class)
    // Hanterar situationer där operationen inte kan utföras i nuvarande tillstånd,
    // t.ex. om man försöker ta bort en tillgång som fortfarande är uthyrd.
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex, HttpServletRequest req) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // ======================================================
    //      404 – Saknas (t.ex. objekt hittas inte)
    // ======================================================

    @ExceptionHandler(java.util.NoSuchElementException.class)
    // Fångar fel där något inte hittas i databasen, t.ex. en kund eller bokning.
    public ResponseEntity<Map<String, Object>> handleNotFound(java.util.NoSuchElementException ex, HttpServletRequest req) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // ======================================================
    //      500 – Oväntat fel (serverfel)
    // ======================================================

    @ExceptionHandler(Exception.class)
    // Fångar alla övriga fel som inte matchas ovan – en "fallback" för oförutsedda problem.
    public ResponseEntity<Map<String, Object>> handleGeneralError(Exception ex, HttpServletRequest req) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Ett oväntat fel inträffade: " + ex.getMessage(), req);
    }

    // ======================================================
    //      Hjälpmetod för att bygga JSON-svar
    // ======================================================

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message, HttpServletRequest req) {
        // Denna metod återanvänds för att bygga enhetliga felmeddelanden.
        Map<String, Object> body = new HashMap<>();

        // Grundläggande information som alla svar har
        body.put("timestamp", LocalDateTime.now());   // När felet inträffade
        body.put("status", status.value());           // HTTP-statuskod (t.ex. 400, 404, 500)
        body.put("error", status.getReasonPhrase());  // Textrepresentation, t.ex. "Bad Request"
        body.put("message", message);                 // Felmeddelandet som skickas vidare
        body.put("path", req.getRequestURI());        // Vilken URL som orsakade felet

        // Returnerar det färdiga svaret som ett ResponseEntity med JSON och UTF-8
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(body);
    }
}
