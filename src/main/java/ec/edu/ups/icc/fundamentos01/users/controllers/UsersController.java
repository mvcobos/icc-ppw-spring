package ec.edu.ups.icc.fundamentos01.users.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.services.UserService;
import jakarta.validation.Valid;

/*
 * Controlador REST encargado de exponer los endpoints HTTP
 * para la gestión de usuarios.
 *
 */
/*
 * Controlador REST encargado de exponer los endpoints HTTP
 * para la gestión de usuarios.
 *
 * En esta práctica el controlador ya no contiene la lógica del CRUD.
 * Solo recibe la petición y delega la operación al servicio.
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserService service;

    /*
     * Inyección de dependencias por constructor.
     *
     * Spring Boot busca una implementación de UserService,
     * encuentra UserServiceImpl porque tiene @Service,
     * crea el objeto y lo inyecta automáticamente.
     */
    public UsersController(UserService service) {
        this.service = service;
    }


    /*
     * Endpoint para listar todos los usuarios.
     *
     * GET /users
     */
    @GetMapping
    public List<UserResponseDto> findAll() {
        return service.findAll();
    }

    /*
     * Endpoint para buscar un usuario por id.
     *
     * GET /users/{id}
     */
    @GetMapping("/{id}")
    public Object findOne(@PathVariable("id") Long id) {
        try {
            return service.findOne(id);
        } catch (IllegalStateException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Object() {
                    public String error = e.getMessage();
                });
        }
    }

    /*
     * @Valid indica que el objeto recibido debe evaluarse con las anotaciones de Jakarta Validation.
     * Si el cliente envía un nombre vacío, un email inválido o una contraseña corta, 
     * Spring Boot detiene la ejecución antes de entrar al servicio.
     */
    @PostMapping
    public Object create(@Valid @RequestBody CreateUserDto dto) {
        try {
            return service.create(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Object() {
                    public String error = e.getMessage();
                });
        }
    }

    /*
     * Endpoint para actualizar completamente un usuario.
     *
     * PUT /users/{id}
     */
    @PutMapping("/{id}")
    public Object update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserDto dto
    ) {
        try {
            return service.update(id, dto);
        } catch (IllegalStateException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Object() {
                    public String error = e.getMessage();
                });
        }
    }

    /*
     * Endpoint para actualizar parcialmente un usuario.
     *
     * PATCH /users/{id}
     */
    @PatchMapping("/{id}")
    public Object partialUpdate(
            @PathVariable("id") Long id,
            @Valid @RequestBody PartialUpdateUserDto dto
    ) {
        try {
            return service.partialUpdate(id, dto);
        } catch (IllegalStateException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Object() {
                    public String error = e.getMessage();
                });
        }
    }
    /*
     * Endpoint para eliminar un usuario.
     *
     * DELETE /users/{id}
     */
    @DeleteMapping("/{id}")
    public Object delete(@PathVariable("id") Long id) {
        try {
            service.delete(id);
            return new Object() {
                public String message = "Deleted successfully";
            };
        } catch (IllegalStateException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Object() {
                    public String error = e.getMessage();
                });
        }
    }
}