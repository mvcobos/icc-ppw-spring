package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado para recibir los datos necesarios
 * para actualizar completamente un producto existente (PUT).
 */
public class UpdateProductDto {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre del producto debe tener entre 3 y 150 caracteres")
    private String name;

    private String description;

    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal price;

    @NotNull(message = "El stock del producto es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    private Integer stock;
    
    public UpdateProductDto() {
    }

    public UpdateProductDto(String name, String description, BigDecimal price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}