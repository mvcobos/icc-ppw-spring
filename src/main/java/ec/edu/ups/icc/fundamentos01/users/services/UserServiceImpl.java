package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.List;
import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.mappers.UserMapper;
import ec.edu.ups.icc.fundamentos01.users.models.UserModel;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Inyección de dependencias por constructor
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Retorna todos los usuarios almacenados en PostgreSQL.
     * El repositorio devuelve entidades, el mapper las convierte a modelos
     * y posteriormente a DTOs de respuesta seguros.
     */
    @Override
    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toModelFromEntity)
                .map(UserMapper::toResponse)
                .toList();
    }

    /*
     * Busca un usuario por id en PostgreSQL usando Optional.
     * Si no existe, lanza un error simple como pide la guía.
     */
    @Override
    public UserResponseDto findOne(Long id) {
        UserEntity entity = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserModel model = UserMapper.toModelFromEntity(entity);
        return UserMapper.toResponse(model);
    }

    /*
     * Crea un nuevo usuario en la base de datos real.
     * Convierte DTO -> Model -> Entity -> Guardado -> Model -> ResponseDto.
     */
    @Override
    public UserResponseDto create(CreateUserDto dto) {
        // Validar que el email no esté registrado
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        
        UserModel model = UserMapper.toModelFromDTO(dto);
        UserEntity entity = UserMapper.toEntityFromModel(model);
        UserEntity savedEntity = userRepository.save(entity);
        UserModel savedModel = UserMapper.toModelFromEntity(savedEntity);
        return UserMapper.toResponse(savedModel);
    }

    /*
     * Actualiza completamente un usuario existente.
     * Modifica los campos en la entidad recuperada y guarda los cambios.
     */
    @Override
    public UserResponseDto update(Long id, UpdateUserDto dto) {
        UserEntity entity = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Validar email duplicado (pero solo si cambió)
        if (!entity.getEmail().equals(dto.getEmail()) && 
            userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());

        UserEntity savedEntity = userRepository.save(entity);
        UserModel model = UserMapper.toModelFromEntity(savedEntity);
        return UserMapper.toResponse(model);
    }

    /*
     * Actualiza parcialmente un usuario (PATCH).
     * Solo reemplaza los campos que no vengan nulos en el DTO.
     */
    @Override
    public UserResponseDto partialUpdate(Long id, PartialUpdateUserDto dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Si está eliminado, lanzar excepción específica
        if (entity.isDeleted()) {
            throw new NotFoundException("Cannot modify a deleted user");
        }

        // Validar email duplicado solo si viene en el DTO y es diferente
        if (dto.getEmail() != null && 
            !entity.getEmail().equals(dto.getEmail()) && 
            userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        if (dto.getPassword() != null && dto.getPassword().length() < 8) {
            throw new ConflictException("Password must be at least 8 characters long");
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null) {
            entity.setPasswordHash("HASH_" + dto.getPassword());
        }

        UserEntity savedEntity = userRepository.save(entity);
        UserModel model = UserMapper.toModelFromEntity(savedEntity);
        return UserMapper.toResponse(model);
    }

    
    /*
     * Elimina lógicamente un usuario por id.
     * Cambia la bandera "deleted" a true sin borrar físicamente la fila.
     */
    @Override
    public void delete(Long id) {
        UserEntity entity = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        entity.setDeleted(true);

        userRepository.save(entity);
    }
}