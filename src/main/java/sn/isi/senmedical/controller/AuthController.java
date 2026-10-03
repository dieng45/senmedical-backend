// AuthController.java
// Expose les routes d'inscription et de connexion (cf. cas d'utilisation "S'inscrire" et "Se connecter")
package sn.isi.senmedical.controller;

import sn.isi.senmedical.dto.AuthResponse;
import sn.isi.senmedical.dto.LoginRequest;
import sn.isi.senmedical.dto.RegisterRequest;
import sn.isi.senmedical.entity.Patient;
import sn.isi.senmedical.entity.Role;
import sn.isi.senmedical.entity.Utilisateur;
import sn.isi.senmedical.repository.UtilisateurRepository;
import sn.isi.senmedical.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UtilisateurRepository utilisateurRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    // POST /api/auth/register : inscription d'un nouveau patient
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        // Scenario alternatif A1 : email deja utilise
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cet email est deja utilise");
        }

        Patient patient = new Patient();
        patient.setNom(request.getNom());
        patient.setPrenom(request.getPrenom());
        patient.setEmail(request.getEmail());
        patient.setMotDePasse(passwordEncoder.encode(request.getMotDePasse())); // hachage BCrypt
        patient.setTelephone(request.getTelephone());
        patient.setDateNaissance(request.getDateNaissance());
        patient.setSexe(request.getSexe());
        patient.setGroupeSanguin(request.getGroupeSanguin());
        patient.setAntecedents(request.getAntecedents());
        patient.setAllergies(request.getAllergies());
        patient.setRole(Role.PATIENT);
        // actif = true par defaut (defini dans Utilisateur.java), pas besoin de le fixer ici

        utilisateurRepository.save(patient);

        String token = jwtUtil.genererToken(patient.getEmail(), patient.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, patient.getEmail(), patient.getRole().name()));
    }

    // POST /api/auth/login : connexion d'un utilisateur (patient ou admin)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
            );
        } catch (Exception e) {
            // Scenario alternatif A1 : identifiants incorrects
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail()).orElseThrow();

        // Scenario alternatif A2 : compte desactive par l'administrateur
        if (!utilisateur.getActif()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ce compte a ete desactive. Contactez l'administrateur.");
        }

        String token = jwtUtil.genererToken(utilisateur.getEmail(), utilisateur.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token, utilisateur.getEmail(), utilisateur.getRole().name()));
    }
}