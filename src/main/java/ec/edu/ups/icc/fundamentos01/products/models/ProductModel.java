package ec.edu.ups.icc.fundamentos01.products.models;

import java.time.LocalDateTime;
import java.util.Set;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;

/**
 * Modelo de dominio del recurso products.
 * * Representa al producto dentro de la lógica de negocio.
 * No es una entidad de base de datos y no debe tener anotaciones JPA.
 */
public class ProductModel {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private UserResponseDto owner;
    private Set<CategoryResponseDto> categories;   
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    public ProductModel() {
    }

    // 12.2 Metodos. Productos ya no usara el mapper, 
    // sino que el dominio sabrá cómo construirse y convertirse a entidad.
    public ProductModel(Long id, String name, String description, Double price, Integer stock, UserResponseDto owner,
            Set<CategoryResponseDto> categories, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.owner = owner;
        this.categories = categories;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    //Crea un ProductModel a partir de un DTO de creación. Es lo que se usa cuando llega un POST. 
    // Recibe los datos del cliente y los pone dentro de un ProductModel.
    public static ProductModel fromDto(CreateProductDto dto) {
        ProductModel product = new ProductModel();
        product.name = dto.getName();
        //product.description = dto.getDescription();
        product.price = dto.getPrice();
        product.stock = dto.getStock();
        product.deleted = false;
        return product;
    }

    /*Crea un ProductModel a partir de una ProductEntity (lo que viene de la base de datos). 
    Cuando se consulta la BD, te devuelve entidades, y este método las convierte a el modelo de dominio. */
    public static ProductModel fromEntity(ProductEntity entity) {
        ProductModel product = new ProductModel();
        product.id = entity.getId();
        product.name = entity.getName();
        //product.description = entity.getDescription();
        product.price = entity.getPrice();
        product.stock = entity.getStock();
        product.owner = new UserResponseDto(
            entity.getOwner().getId(),
            entity.getOwner().getName(),
            entity.getOwner().getEmail(),
            entity.getOwner().getCreatedAt()
        );
        product.categories = entity.getCategories().stream().map(
            categoryEntity -> new CategoryResponseDto(
                categoryEntity.getId(),
                categoryEntity.getName(),
                categoryEntity.getDescription()
            )
        ).collect(java.util.stream.Collectors.toSet());       
        product.createdAt = entity.getCreatedAt();
        product.updatedAt = entity.getUpdatedAt();
        product.deleted = entity.isDeleted();
        return product;
    }

    /* Convierte el ProductModel actual en una ProductEntity para guardarlo en la base de datos. 
    Es lo opuesto de fromEntity.*/
    public ProductEntity toEntity() {
        ProductEntity entity = new ProductEntity();
        if (this.id != null) {
            entity.setId(this.id);
        }
        entity.setName(this.name);
        //entity.setDescription(this.description);
        entity.setPrice(this.price);
        entity.setStock(this.stock);
        entity.setDeleted(this.deleted);
        return entity;
    }

    /*Convierte el ProductModel actual en un DTO de respuesta para devolver al cliente. 
    Solo expone los campos seguros (no expone, por ejemplo, el campo deleted). */
    public ProductResponseDto toResponseDto() {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(this.id);
        dto.setName(this.name);
        //dto.setDescription(this.description);
        dto.setPrice(this.price);
        dto.setStock(this.stock);
        dto.setOwner(this.owner);
        
        dto.setCategories(this.categories);
        dto.setCreatedAt(this.createdAt);
        dto.setUpdatedAt(this.updatedAt);
        return dto;
    }

    /*Actualiza todos los campos editables del ProductModel actual. 
    Se usa para PUT (actualización completa). */
    public void update(UpdateProductDto dto) {
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.price = dto.getPrice();
        this.stock = dto.getStock();
    }

    /*Actualiza solo los campos que no son null del ProductModel actual. 
    Se usa para PATCH (actualización parcial). */
    public void partialUpdate(PartialUpdateProductDto dto) {
        if (dto.getName() != null) {
            this.name = dto.getName();
        }
        // if (dto.getDescription() != null) {
        //     this.description = dto.getDescription();
        // }
        if (dto.getPrice() != null) {
            this.price = dto.getPrice();
        }
        if (dto.getStock() != null) {
            this.stock = dto.getStock();
        }
    }
    
    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public String getDescription() { 
        return description; 
    }

    public void setDescription(String description) { 
        this.description = description; 
    }

    public Double getPrice() { 
        return price; 
    }

    public void setPrice(Double price) { 
        this.price = price; 
    }

    public Integer getStock() { 
        return stock; 
    }

    public void setStock(Integer stock) { 
        this.stock = stock; 
    }

    public UserResponseDto getOwner() {
        return owner;
    }

    public void setOwner(UserResponseDto owner) {
        this.owner = owner;
    }

    public Set<CategoryResponseDto> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryResponseDto> categories) {
        this.categories = categories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}