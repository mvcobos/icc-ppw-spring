package ec.edu.ups.icc.fundamentos01.users.entities;

import java.util.HashSet;
import java.util.Set;

import ec.edu.ups.icc.fundamentos01.core.entities.BaseEntity;
import ec.edu.ups.icc.fundamentos01.security.entities.RoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/*
 * Entidad JPA del recurso users.
 *
 * Representa la tabla users en PostgreSQL.
 * Esta clase sí pertenece a la capa de persistencia.
 */
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

     // ============== NUEVA RELACIÓN CON ROLES ==============

    /**
     * Relación ManyToMany con Roles
     * 
     * @ManyToMany: Un usuario puede tener múltiples roles
     *              Un rol puede estar asignado a múltiples usuarios
     * 
     * fetch = FetchType.EAGER:
     *   - Carga los roles AUTOMÁTICAMENTE al consultar el usuario
     *   - Necesario porque Spring Security necesita los roles al autenticar
     *   - Sin EAGER, tendríamos LazyInitializationException al acceder a roles
     * 
     * @JoinTable: Crea tabla intermedia user_roles
     *   name = "user_roles": Nombre de la tabla intermedia en BD
     *   joinColumns: Columna que referencia a esta entidad (users.id)
     *   inverseJoinColumns: Columna que referencia a RoleEntity (roles.id)
     * 
     * Tabla user_roles en BD:
     *   CREATE TABLE user_roles (
     *       user_id BIGINT REFERENCES users(id),
     *       role_id BIGINT REFERENCES roles(id),
     *       PRIMARY KEY (user_id, role_id)
     *   );
     * 
     * Set<RoleEntity>:
     *   - Set (no List) evita duplicados
     *   - HashSet inicializado para evitar NullPointerException
     * 
     * Ejemplo de uso:
     *   UserEntity user = userRepository.findById(1L);
     *   user.getRoles();  // ← Ya cargados por EAGER
     *   // → [RoleEntity(ROLE_USER), RoleEntity(ROLE_ADMIN)]
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    // Constructor vacío
    public UserEntity() {
    }

    // Constructor lleno
    public UserEntity(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }
}