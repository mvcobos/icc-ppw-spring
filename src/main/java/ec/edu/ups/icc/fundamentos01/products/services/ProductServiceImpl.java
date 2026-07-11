package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByCategoryDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByUserDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.models.ProductModel;
import ec.edu.ups.icc.fundamentos01.products.repositories.ProductRepository;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    /*
     * Lista blanca de campos permitidos para ordenar.
     *
     * Evita ordenar por relaciones (owner, categories) no preparadas
     * para esta práctica.
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "price", "stock", "createdAt", "updatedAt"
    );

    private final ProductRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository repository, UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // Retorna todos los productos no eliminados.
    // Convierte las entidades a DTOs de respuesta.
    @Override
    public List<ProductResponseDto> findAll() {
        return repository.findAll().stream()
                .filter(entity -> !entity.isDeleted())
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto)
                .toList();
    }

    // Busca un producto por su id.
    // Lanza una excepción si no se encuentra.
    @Override
    public Object findOne(Long id) {
        ProductEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return ProductModel.fromEntity(entity).toResponseDto();
    }

    // Busca los productos no eliminados que pertenecen a una categoría.
    // Lanza una excepción si la categoría no existe.
    @Override
    public List<ProductResponseDto> findByCategory(Long categoryId) {
        if (!categoryRepository.existsByIdAndIsDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        return repository.findByCategoriesIdAndIsDeletedFalse(categoryId).stream()
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto)
                .toList();
    }

    // Busca los productos no eliminados que pertenecen a un usuario.
    // Lanza una excepción si el usuario no existe.
    @Override
    public List<ProductResponseDto> findByUser(Long userId) {
        if (!userRepository.existsByIdAndIsDeletedFalse(userId)) {
            throw new NotFoundException("User not found");
        }

        return repository.findByOwnerIdAndIsDeletedFalse(userId).stream()
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto)
                .toList();
    }

    /*
    * Retorna productos activos de un usuario aplicando filtros opcionales.
    *
    * Primero valida que el usuario exista y no esté eliminado.
    * Luego valida el rango de precios.
    * Finalmente consulta los productos desde ProductRepository.
    */
    @Override
    public List<ProductResponseDto> findByUserIdWithFilters(
            Long userId,
            ProductFilterByUserDto filters
    ) {
        if (!userRepository.existsByIdAndIsDeletedFalse(userId)) {
            throw new NotFoundException("User not found");
        }

        validateUserFilters(filters);

        String name = normalizeName(filters.getName());

        return repository.findByOwnerIdWithFilters(
                        userId,
                        name,
                        filters.getMinPrice(),
                        filters.getMaxPrice(),
                        filters.getCategoryId()
                )
                .stream()
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto)
                .toList();
    }

    /*
    * Retorna productos activos de una categoría aplicando filtros opcionales.
    *
    * Primero valida que la categoría exista y no esté eliminada.
    * Luego valida el rango de precios.
    * Finalmente consulta los productos desde ProductRepository.
    */
    @Override
    public List<ProductResponseDto> findByCategoryIdWithFilters(
            Long categoryId,
            ProductFilterByCategoryDto filters
    ) {
        if (!categoryRepository.existsByIdAndIsDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        validateCategoryFilters(filters);

        String name = normalizeName(filters.getName());

        return repository.findByCategoryIdWithFilters(
                        categoryId,
                        name,
                        filters.getMinPrice(),
                        filters.getMaxPrice(),
                        filters.getUserId()
                )
                .stream()
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto)
                .toList();
    }

    /*
     * Retorna productos activos usando Page.
     *
     * Incluye metadatos completos:
     * totalElements, totalPages, number, size, first, last.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllPage(PaginationDto pagination) {

        Pageable pageable = createPageable(pagination);

        return repository.findActivePage(pageable)
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto);
    }

    /*
     * Retorna, usando Slice, únicamente los productos activos
     * que pertenecen al usuario autenticado.
     *
     * El filtro por owner se hace en la consulta del repositorio,
     * no se traen todos los productos para descartarlos después.
     */
    @Override
    @Transactional(readOnly = true)
    public Slice<ProductResponseDto> findAllSlice(PaginationDto pagination, UserDetailsImpl currentUser) {

        Pageable pageable = createPageable(pagination);

        return repository.findActiveSliceByOwnerId(currentUser.getId(), pageable)
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto);
    }

    /*
     * Retorna productos activos de una categoría usando Page.
     *
     * Mantiene los filtros existentes y agrega paginación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findByCategoryIdWithFiltersPage(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    ) {
        if (!categoryRepository.existsByIdAndIsDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        validateCategoryFilters(filters);

        String name = normalizeName(filters.getName());

        Pageable pageable = createPageable(pagination);

        return repository.findByCategoryIdWithFiltersPage(
                        categoryId,
                        name,
                        filters.getMinPrice(),
                        filters.getMaxPrice(),
                        filters.getUserId(),
                        pageable
                )
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto);
    }

    /*
     * Retorna productos activos de una categoría usando Slice.
     *
     * No calcula totalElements ni totalPages.
     */
    @Override
    @Transactional(readOnly = true)
    public Slice<ProductResponseDto> findByCategoryIdWithFiltersSlice(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    ) {
        if (!categoryRepository.existsByIdAndIsDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        validateCategoryFilters(filters);

        String name = normalizeName(filters.getName());

        Pageable pageable = createPageable(pagination);

        return repository.findByCategoryIdWithFiltersSlice(
                        categoryId,
                        name,
                        filters.getMinPrice(),
                        filters.getMaxPrice(),
                        filters.getUserId(),
                        pageable
                )
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto);
    }

    /*
    * Valida reglas de negocio relacionadas con filtros de usuario.
    */
    private void validateUserFilters(ProductFilterByUserDto filters) {

        if (filters == null) {
            return;
        }

        if (!filters.hasValidPriceRange()) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }

        if (filters.getCategoryId() != null &&
                !categoryRepository.existsByIdAndIsDeletedFalse(filters.getCategoryId())) {
            throw new NotFoundException("Category not found");
        }
    }

    /*
    * Valida reglas de negocio relacionadas con filtros de categoría.
    */
    private void validateCategoryFilters(ProductFilterByCategoryDto filters) {

        if (filters == null) {
            return;
        }

        if (!filters.hasValidPriceRange()) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }

        if (filters.getUserId() != null &&
                !userRepository.existsByIdAndIsDeletedFalse(filters.getUserId())) {
            throw new NotFoundException("User not found");
        }
    }

    /*
    * Convierte un texto vacío en null.
    *
    * Esto permite que el repositorio ignore el filtro por nombre
    * cuando el query param llega vacío.
    */
    private String normalizeName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim();
    }

    /*
     * Construye el objeto Pageable validando:
     * página, tamaño, campo de ordenamiento y dirección.
     */
    private Pageable createPageable(PaginationDto pagination) {

        String sortBy = normalizeSortBy(pagination.getSortBy());

        Sort.Direction direction = normalizeDirection(pagination.getDirection());

        Sort sort = Sort.by(direction, sortBy);

        return PageRequest.of(
                pagination.getPage(),
                pagination.getSize(),
                sort
        );
    }

    /*
     * Valida que el campo de ordenamiento exista y esté permitido.
     *
     * Se usa lista blanca para evitar ordenar por campos inexistentes
     * o por relaciones complejas no preparadas para esta práctica.
     */
    private String normalizeSortBy(String sortBy) {

        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Campo de ordenamiento no permitido: " + sortBy);
        }

        return sortBy;
    }

    /*
     * Convierte la dirección recibida por query param
     * en Sort.Direction.
     */
    private Sort.Direction normalizeDirection(String direction) {

        if (direction == null || direction.isBlank()) {
            return Sort.Direction.ASC;
        }

        if (direction.equalsIgnoreCase("asc")) {
            return Sort.Direction.ASC;
        }

        if (direction.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }

        throw new BadRequestException("Dirección de ordenamiento no válida: " + direction);
    }

    // POST
    // Crea un nuevo producto usando como owner al usuario autenticado.
    // El owner ya no se toma del DTO, así se evita que un usuario
    // cree productos a nombre de otro.
    @Override
    public ProductResponseDto create(CreateProductDto dto, UserDetailsImpl currentUser) {
        UserEntity owner = findCurrentUserEntity(currentUser);

        Set<CategoryEntity> categories = resolveCategories(dto.getCategoryIds());

        ProductModel model = ProductModel.fromDto(dto);
        ProductEntity entity = model.toEntity();
        entity.setOwner(owner);
        entity.setCategories(categories);
        ProductEntity savedEntity = repository.save(entity);
        return ProductModel.fromEntity(savedEntity).toResponseDto();
    }

    // PUT
    // Actualiza completamente un producto existente, incluidas sus categorías.
    // Solo el dueño del producto o un ADMIN pueden modificarlo.
    @Override
    public Object update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser) {
        ProductEntity existing = findActiveProductOrThrow(id);

        validateOwnership(existing, currentUser);

        Set<CategoryEntity> categories = resolveCategories(dto.getCategoryIds());

        ProductModel model = ProductModel.fromEntity(existing);
        model.update(dto);

        ProductEntity updated = model.toEntity();
        updated.setId(id);
        updated.setOwner(existing.getOwner());
        updated.setCategories(categories);
        updated.setCreatedAt(existing.getCreatedAt());
        ProductEntity saved = repository.save(updated);
        return ProductModel.fromEntity(saved).toResponseDto();
    }

    // PATCH
    // Actualiza parcialmente un producto existente.
    // Solo reemplaza las categorías si el cliente las envía.
    // Solo el dueño del producto o un ADMIN pueden modificarlo.
    @Override
    public Object partialUpdate(Long id, PartialUpdateProductDto dto, UserDetailsImpl currentUser) {
        ProductEntity existing = findActiveProductOrThrow(id);

        validateOwnership(existing, currentUser);

        ProductModel model = ProductModel.fromEntity(existing);
        model.partialUpdate(dto);

        Set<CategoryEntity> categories = dto.getCategoryIds() != null
                ? resolveCategories(dto.getCategoryIds())
                : existing.getCategories();

        ProductEntity updated = model.toEntity();
        updated.setId(id);
        updated.setOwner(existing.getOwner());
        updated.setCategories(categories);
        updated.setCreatedAt(existing.getCreatedAt());
        ProductEntity saved = repository.save(updated);
        return ProductModel.fromEntity(saved).toResponseDto();
    }

    // DELETE
    // Elimina lógicamente un producto por su id.
    // Solo el dueño del producto o un ADMIN pueden eliminarlo.
    @Override
    public void delete(Long id, UserDetailsImpl currentUser) {
        ProductEntity existing = findActiveProductOrThrow(id);

        validateOwnership(existing, currentUser);

        existing.setDeleted(true);
        repository.save(existing);
    }

    /*
     * Busca un producto activo.
     *
     * Si no existe o está eliminado, devuelve 404.
     */
    private ProductEntity findActiveProductOrThrow(Long id) {
        return repository.findById(id)
                .filter(product -> !product.isDeleted())
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    /*
     * Obtiene el usuario autenticado como entidad JPA.
     *
     * currentUser viene desde el token JWT.
     * Se consulta en base para asegurar que siga existiendo
     * y no esté eliminado lógicamente.
     */
    private UserEntity findCurrentUserEntity(UserDetailsImpl currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        return userRepository.findByIdAndIsDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuario no autorizado"));
    }

    /*
     * Valida si el usuario autenticado puede modificar o eliminar el producto.
     *
     * ROLE_ADMIN puede modificar cualquier producto.
     * ROLE_USER solo puede modificar productos propios.
     */
    private void validateOwnership(ProductEntity product, UserDetailsImpl currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (hasRole(currentUser, "ROLE_ADMIN")) {
            return;
        }

        if (product.getOwner() == null || product.getOwner().getId() == null) {
            throw new AccessDeniedException("El producto no tiene propietario válido");
        }

        if (!product.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No puedes modificar productos ajenos");
        }
    }

    /*
     * Verifica si el usuario tiene un rol específico.
     *
     * Las authorities vienen desde UserDetailsImpl.
     */
    private boolean hasRole(UserDetailsImpl user, String role) {
        return user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }

    /*
     * Resuelve y valida el conjunto de categorías a partir de sus ids.
     *
     * Lanza una excepción si alguna categoría no existe o está eliminada.
     */
    private Set<CategoryEntity> resolveCategories(Set<Long> categoryIds) {
        Set<CategoryEntity> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));

        if (categories.size() != categoryIds.size()) {
            throw new NotFoundException("Category not found");
        }

        for (CategoryEntity category : categories) {
            if (category.isDeleted()) {
                throw new NotFoundException("Category not found");
            }
        }

        return categories;
    }
}
