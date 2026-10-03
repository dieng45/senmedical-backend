// JwtUtil.java
// Utilitaire pour generer, valider et lire les informations d'un token JWT
package sn.isi.senmedical.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Lue depuis application.properties (app.jwt.secret)
    @Value("${app.jwt.secret}")
    private String secret;

    // Duree de validite du token en millisecondes (app.jwt.expiration)
    @Value("${app.jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Genere un token contenant l'email de l'utilisateur et son role
    public String genererToken(String email, String role) {
        Date maintenant = new Date();
        Date expirationDate = new Date(maintenant.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(maintenant)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Extrait l'email (subject) contenu dans le token
    public String extraireEmail(String token) {
        return extraireClaims(token).getSubject();
    }

    // Extrait le role contenu dans le token
    public String extraireRole(String token) {
        return extraireClaims(token).get("role", String.class);
    }

    private Claims extraireClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Verifie que le token est valide (signature correcte et non expire)
    public boolean estValide(String token) {
        try {
            extraireClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}