# NOTAS EXTRA

## PRACTICA 6: 
**Validaciones:**  
    String = `@NotBlank` y `@Size`  
    BigDecimal = `@NotNull` y `@DecimalMin`  
    Integer = `@NotNull` y `@Min`

**¿Por qué 3 DTOs distintos?**  
*CreateProductDto* → para crear (POST). Todo es obligatorio.  
*UpdateProductDto* → para actualizar completo (PUT). Todo es obligatorio.  
*PartialUpdateProductDto* → para actualizar parcial (PATCH). Los campos son opcionales (solo actualizas lo que mandas).

### *PASO 12.2: Modelo de dominio Product*
**¿Por qué ya no usamos Mapper?**  
Antes el mapper hacía las conversiones. Ahora el modelo sabe convertirse a sí mismo. Esto se llama "domain-driven design" — el dominio tiene responsabilidad sobre sus propias transformaciones.

---
### DATOS: 
- Versión de Java utilizada: **21**
- Versión de Spring Boot: **4.1.0**
- Build Tool: **Gradle**
- Servidor embebido: **Tomcat 11.0.x**
- Puerto del servidor: **8080**

---