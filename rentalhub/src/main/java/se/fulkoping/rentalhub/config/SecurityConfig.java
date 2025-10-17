package se.fulkoping.rentalhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import se.fulkoping.rentalhub.filter.ApiKeyFilter;

@Configuration
// Markerar klassen som en konfigurationsklass. Spring skannar den här klassen för att hitta och registrera beans.
public class SecurityConfig {

    @Bean
    // Den här metoden definierar en bean av typen SecurityFilterChain. Spring anropar metoden vid uppstart
    // och registrerar det returnerade objektet i applikationens kontext.
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyFilter apiKeyFilter) throws Exception {
        // Metoden tar emot ett HttpSecurity-objekt (som Spring injicerar) och vårt ApiKeyFilter.
        // Vi kan kasta Exception eftersom build() kan kasta undantag om konfigurationen är ogiltig.

        http
                // Start på ett kedjeanrop där vi konfigurerar olika säkerhetsaspekter.
                .csrf(csrf -> csrf.disable())
                // Inaktiverar CSRF-skyddet. För rena API:er som inte använder cookies/sessions
                // och där klienter skickar token/nyckel i header är det vanligt att stänga av CSRF.
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                // Lägger in vårt eget ApiKeyFilter före det inbyggda UsernamePasswordAuthenticationFilter i filterkedjan.
                // Detta gör att vår API-nyckelkontroll sker tidigt för alla relevanta requests.
                .authorizeHttpRequests(auth -> auth
                                // Börjar konfigurera auktoriseringsregler för inkommande HTTP-förfrågningar.
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**"
                                ).permitAll()
                                // Tillåter åtkomst utan autentisering till Swagger UI och OpenAPI-endpoints
                                // så att dokumentationen är läsbar i webbläsaren.
                                .anyRequest().permitAll() // ändrat här!
                        // Tillåter alla övriga requests utan krav på autentisering.
                        // Om man vill kräva API-nyckel för POST/PUT/DELETE men inte för GET
                        // kan man låta ApiKeyFilter själv göra kontrollen på metoden,
                        // och här istället sätta .anyRequest().authenticated() om så önskas.
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                // Inaktiverar HTTP Basic Authentication (användarnamn/lösen via webbläsarens dialog).
                .formLogin(form -> form.disable());
        // Inaktiverar formulärinloggning (webbformulär). Det här är ett API och ska normalt inte använda HTML-inloggning.

        return http.build();
        // Bygger och returnerar den konfigurerade SecurityFilterChain som Spring sedan använder för att säkra appen.
    }
}
