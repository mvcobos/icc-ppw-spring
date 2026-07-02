package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.models.ProductModel;
import ec.edu.ups.icc.fundamentos01.products.repositories.ProductRepository;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

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

        return repository.findByCategoryIdAndIsDeletedFalse(categoryId).stream()
                .map(ProductModel::fromEntity)
                .map(ProductModel::toResponseDto)
                .toList();
    }

    // Crea un nuevo producto a partir de un DTO.
    // Guarda la entidad y devuelve el DTO de respuesta.
    @Override
    public ProductResponseDto create(CreateProductDto dto) {
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        ProductModel model = ProductModel.fromDto(dto);
        ProductEntity entity = model.toEntity();
        entity.setOwner(user);
        entity.setCategory(category);
        ProductEntity savedEntity = repository.save(entity);
        return ProductModel.fromEntity(savedEntity).toResponseDto();
    }

    // Actualiza completamente un producto existente.
    // Lanza una excepción si el producto está eliminado o no se encuentra.
    @Override
    public Object update(Long id, UpdateProductDto dto) {
        ProductEntity existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
                
        if (existing.isDeleted()) {
            throw new NotFoundException("Product not found");
        }

        ProductModel model = ProductModel.fromEntity(existing);
        model.update(dto);

        ProductEntity updated = model.toEntity();
        updated.setId(id);
        updated.setOwner(existing.getOwner());
        updated.setCategory(existing.getCategory());
        ProductEntity saved = repository.save(updated);
        return ProductModel.fromEntity(saved).toResponseDto();
    }

    // Actualiza parcialmente un producto existente.
    // Lanza una excepción si el producto está eliminado o no se encuentra.
    @Override
    public Object partialUpdate(Long id, PartialUpdateProductDto dto) {
        ProductEntity existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (existing.isDeleted()) {
            throw new NotFoundException("Product not found");
        }

        ProductModel model = ProductModel.fromEntity(existing);
        model.partialUpdate(dto);

        ProductEntity updated = model.toEntity();
        updated.setId(id);
        updated.setOwner(existing.getOwner());
        updated.setCategory(existing.getCategory());
        ProductEntity saved = repository.save(updated);
        return ProductModel.fromEntity(saved).toResponseDto();
    }

    // Elimina lógicamente un producto por su id.
    // Lanza una excepción si el producto ya está eliminado o no se encuentra.
    @Override
    public void delete(Long id) {
        ProductEntity existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (existing.isDeleted()) {
            throw new NotFoundException("Product not found");
        }

        existing.setDeleted(true);
        repository.save(existing);
    }
}