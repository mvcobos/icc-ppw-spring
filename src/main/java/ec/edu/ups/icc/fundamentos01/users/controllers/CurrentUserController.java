package ec.edu.ups.icc.fundamentos01.users.controllers;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.security.dtos.CurrentUserResponseDto;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;

/*
 * Controlador REST para consultar información
 * del usuario autenticado.
 *
 * La ruta final es:
 * GET /api/users/me
 */
@RestController
@RequestMapping("/users")
public class CurrentUserController {

    /*
     * Retorna los datos del usuario autenticado.
     *
     * @AuthenticationPrincipal obtiene el usuario que fue colocado
     * en el SecurityContext por JwtAuthenticationFilter.
     */
    @GetMapping("/me")
    public CurrentUserResponseDto me(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        Set<String> roles = currentUser.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new CurrentUserResponseDto(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getEmail(),
                roles
        );
    }
}
