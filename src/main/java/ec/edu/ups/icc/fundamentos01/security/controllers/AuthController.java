package ec.edu.ups.icc.fundamentos01.security.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "Autenticación",
    description = "Endpoints para autenticación y gestión de tokens JWT"
)
@RestController
@RequestMapping("/auth") // Prefijo para todos los endpoints de autenticación
public class AuthController {

    private final AuthService authService; // Servicio de lógica de autenticación

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login - Endpoint público (configurado en SecurityConfig)
     * POST /auth/login
     */
    @Operation(
        summary = "Login de usuario",
        description = "Autentica a un usuario y devuelve tokens JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario autenticado exitosamente"),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        // @Valid valida anotaciones en LoginRequestDto (email, password requeridos)
        AuthResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response); // 200 OK con JWT
    }

    /**
     * Registro - Endpoint público (configurado en SecurityConfig)
     * POST /auth/register
     */
    @Operation(
        summary = "Registro de usuario",
        description = "Crea un nuevo usuario, asigna ROLE_USER y devuelve tokens JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Usuario creado exitosamente"),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos o email ya registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        // @Valid valida anotaciones en RegisterRequestDto
        AuthResponseDto response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created con JWT
    }

    /*
     * Refresh.
     *
     * Recibe un refresh token válido y devuelve nuevos tokens.
     */
    @Operation(
        summary = "Refresh de tokens",
        description = "Recibe un refresh token válido y devuelve nuevos tokens JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tokens actualizados exitosamente"),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        AuthResponseDto response = authService.refresh(request);

        return ResponseEntity.ok(response);
    }

    /*
     * Logout.
     *
     * Revoca el refresh token recibido.
     */
    @Operation(
        summary = "Logout de usuario",
        description = "Revoca el refresh token recibido."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Logout exitoso"),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        authService.logout(request);
    }
}
