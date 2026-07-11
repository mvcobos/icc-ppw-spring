**¿Qué es un DTO?**  
DTO = "Data Transfer Object". Es lo que viaja entre el cliente (Postman) y el API. NO se guarda en BD. Es un "contrato" de lo que se acepta o devuelve.

---
*PASO 12.2: Modelo de dominio Product*  
**¿Qué es un modelo de dominio?**  
Es la clase que representa al producto dentro de tu aplicación. NO es lo que llega del cliente (DTO) ni lo que se guarda en BD (Entity), es el "producto puro" con su lógica propia.
s
---

**Token**

---
## Tabla resumen de conceptos

| Concepto | Descripción | Ejemplo |
|----------|-------------|---------|
| `interface` | Define un conjunto de métodos que una clase debe implementar, pero no especifica cómo deben ser implementados. No se puede instanciar directamente. | `public interface ProductRepository extends JpaRepository<ProductEntity, Long>` |
| `implements` | Indica que una clase proporciona la implementación (cuerpo) de los métodos definidos en una interfaz. | `public class ProductServiceImpl implements ProductService` |
| `abstract class` | Una clase que puede contener métodos abstractos (sin implementación) y métodos concretos (con implementación). No se puede instanciar directamente, pero otras clases pueden extenderla. | `public abstract class BaseEntity` |
| `extends` | Indica que una clase (subclase) hereda los atributos y métodos de otra clase (superclase). La subclase es un tipo más específico de la superclase. | `public class Perro extends Animal` `public class ProductEntity extends BaseEntity`|
| `JpaRepository<T, ID>` | Una interfaz de Spring Data JPA que define métodos comunes para operaciones CRUD en una entidad. | `public interface ProductRepository extends JpaRepository<ProductEntity, Long>` |