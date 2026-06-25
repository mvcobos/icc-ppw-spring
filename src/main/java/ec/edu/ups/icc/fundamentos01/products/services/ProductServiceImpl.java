package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.models.ProductModel;
import ec.edu.ups.icc.fundamentos01.products.repositories.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
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
                .orElseThrow(() -> new IllegalStateException("Product not found"));
        return ProductModel.fromEntity(entity).toResponseDto();
    }

    // Crea un nuevo producto a partir de un DTO.
    // Guarda la entidad y devuelve el DTO de respuesta.
    @Override
    public ProductResponseDto create(CreateProductDto dto) {
        ProductModel model = ProductModel.fromDto(dto);
        ProductEntity entity = model.toEntity();
        ProductEntity savedEntity = repository.save(entity);
        return ProductModel.fromEntity(savedEntity).toResponseDto();
    }

    // Actualiza completamente un producto existente.
    // Lanza una excepción si el producto está eliminado o no se encuentra.
    @Override
    public Object update(Long id, UpdateProductDto dto) {
        ProductEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product not found"));
                
        if (existing.isDeleted()) {
            throw new IllegalStateException("Cannot update a deleted product");
        }

        ProductModel model = ProductModel.fromEntity(existing);
        model.update(dto);
        
        ProductEntity updated = model.toEntity();
        updated.setId(id);
        ProductEntity saved = repository.save(updated);
        return ProductModel.fromEntity(saved).toResponseDto();
    }

    // Actualiza parcialmente un producto existente.
    // Lanza una excepción si el producto está eliminado o no se encuentra.
    @Override
    public Object partialUpdate(Long id, PartialUpdateProductDto dto) {
        ProductEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        if (existing.isDeleted()) {
            throw new IllegalStateException("Cannot update a deleted product");
        }

        ProductModel model = ProductModel.fromEntity(existing);
        model.partialUpdate(dto);
        
        ProductEntity updated = model.toEntity();
        updated.setId(id);
        ProductEntity saved = repository.save(updated);
        return ProductModel.fromEntity(saved).toResponseDto();
    }

    // Elimina lógicamente un producto por su id.
    // Lanza una excepción si el producto ya está eliminado o no se encuentra.
    @Override
    public void delete(Long id) {
        ProductEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        if (existing.isDeleted()) {
            throw new IllegalStateException("Product is already deleted");
        }

        existing.setDeleted(true);
        repository.save(existing);
    }
}