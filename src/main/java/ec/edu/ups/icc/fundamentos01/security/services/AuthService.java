package ec.edu.ups.icc.fundamentos01.security.services;
// imports packages y clases....
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.fundamentos01.security.entities.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.enums.RoleName;
import ec.edu.ups.icc.fundamentos01.security.repositories.RoleRepository;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    // Dependencias inyectadas para login y registro
    private final AuthenticationManager authenticationManager; // Valida credenciales
    private final UserRepository userRepository;               // Acceso a BD
    private final RoleRepository roleRepository;               // Gestión de roles
    private final PasswordEncoder passwordEncoder;             // Hash de passwords
    private final JwtUtil jwtUtil;                            // Generación de tokens
    private final RefreshTokenService refreshTokenService;     // Gestión de refresh tokens

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Login:
     *
     * 1. Valida credenciales.
     * 2. Genera access token.
     * 3. Revoca refresh tokens anteriores.
     * 4. Genera refresh token nuevo.
     * 5. Devuelve ambos tokens al cliente.
     */
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {

        // 1. Validar email y password con Spring Security
        // authenticationManager usa UserDetailsService internamente
        // Si falla: lanza BadCredentialsException → 401
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        // 2. Establecer usuario autenticado en contexto de seguridad
        // Permite acceso a usuario actual en servicios
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generar JWT con datos del usuario
        String accessToken = jwtUtil.generateAccessToken(authentication);

        // 4. Extraer información del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        UserEntity user = findActiveUserById(userDetails.getId());

        /*
         * En esta práctica se deja una sola sesión activa por usuario.
         * Por eso se revocan refresh tokens anteriores.
         */
        refreshTokenService.revokeAllByUser(user);

        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                user,
                userDetails
        );

        // 5. Retornar JWT (access y refresh) + datos del usuario
        return buildAuthResponse(
                accessToken,
                refreshToken.getToken(),
                user
        );
    }

    /**
     * Registro:
     *
     * 1. Crea el usuario.
     * 2. Asigna ROLE_USER.
     * 3. Genera access token.
     * 4. Genera refresh token.
     */
    @Transactional // Requiere transacción para INSERT
    public AuthResponseDto register(RegisterRequestDto registerRequest) {

        // 1. Validar que email no exista
        // Si existe: lanza ConflictException → 409
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        // 2. Crear nueva entidad de usuario
        UserEntity user = new UserEntity();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        // Hash del password con BCrypt (nunca almacenar en texto plano)
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        // 3. Asignar rol por defecto ROLE_USER
        // Si no existe: lanza BadRequestException → 400
        RoleEntity userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow(() -> new BadRequestException("Rol por defecto no encontrado"));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // 4. Guardar en BD (INSERT)
        UserEntity savedUser = userRepository.save(user);

        // 5. Generar access token y refresh token
        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String accessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);

        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                savedUser,
                userDetails
        );

        // 6. Retornar JWT (access y refresh) + datos del usuario registrado
        return buildAuthResponse(
                accessToken,
                refreshToken.getToken(),
                savedUser
        );
    }

    /**
     * Refresh:
     *
     * 1. Valida el refresh token recibido.
     * 2. Revoca el refresh token usado.
     * 3. Genera nuevo access token.
     * 4. Genera nuevo refresh token.
     *
     * Esto se llama rotación de refresh token.
     */
    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {

        RefreshTokenEntity currentRefreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        UserEntity user = currentRefreshToken.getUser();

        refreshTokenService.revoke(currentRefreshToken);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        String newAccessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);

        RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(
                user,
                userDetails
        );

        return buildAuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                user
        );
    }

    /**
     * Logout:
     *
     * Revoca el refresh token enviado.
     *
     * Después de esto, ese refresh token ya no podrá usarse
     * para renovar sesión.
     */
    @Transactional
    public void logout(RefreshTokenRequestDto request) {

        RefreshTokenEntity refreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        refreshTokenService.revoke(refreshToken);
    }

    /**
     * Busca un usuario activo por id.
     */
    private UserEntity findActiveUserById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BadRequestException("Usuario no válido"));
    }

    /**
     * Construye la respuesta de autenticación.
     */
    private AuthResponseDto buildAuthResponse(
            String accessToken,
            String refreshToken,
            UserEntity user
    ) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponseDto(
                accessToken,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles,
                refreshToken
        );
    }
}
