// AuthResponse.java
// Reponse renvoyee apres une inscription ou une connexion reussie
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
}