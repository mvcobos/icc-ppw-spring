package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado para recibir los datos que se desean
 * actualizar parcialmente en un producto existente (PATCH).
 */
public class PartialUpdateProductDto {

    @Size(min = 3, max = 150, message = "El nombre del producto debe tener entre 3 y 150 caracteres")
    private String name;

    private String description;

    @DecimalMin(value = "0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal price;


    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    private Integer stock;

    public PartialUpdateProductDto() {
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