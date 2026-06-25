## **Capas de la Aplicación**

1. **Capa de Presentación (Controlador)**: Maneja las peticiones HTTP y las respuestas.
2. **Capa de Negocio (Servicio)**: Contiene la lógica de negocio de la aplicación.
3. **Capa de Persistencia (Repositorio)**: Maneja la comunicación con la base de datos.
4. **Capa de Dominio (Modelo)**: Representa las entidades y reglas de negocio.

Los DTOs (Data Transfer Objects) y las Entidades no pertenecen a una capa específica, sino que son estructuras de datos que se mueven entre las capas.

---

## **Responsabilidades de cada Componente**

### **1. Controlador (ProductsController)**
- **Capa**: Presentación
- **Responsabilidades**:
  - Recibir las peticiones HTTP (GET, POST, PUT, DELETE...)
  - Validar los datos de entrada (DTOs) con `@Valid`
  - Llamar al servicio correspondiente para procesar la petición
  - Devolver la respuesta HTTP con los datos (DTOs) o errores
- **Dependencias**: Servicio

### **2. Servicio (ProductService, ProductServiceImpl)**
- **Capa**: Negocio
- **Responsabilidades**:
  - Implementar la lógica de negocio de las operaciones (crear, leer, actualizar, eliminar...)
  - Validar reglas de negocio (no permitir actualizar productos eliminados, por ejemplo)
  - Orquestar las llamadas a repositorios y otros servicios si es necesario
  - Manejar transacciones si se requiere
- **Dependencias**: Repositorio, otros Servicios (si es necesario), Mapper (ahora ya no porque se usa el modelo)

### **3. Repositorio (ProductRepository)**
- **Capa**: Persistencia
- **Responsabilidades**:
  - Comunicarse con la base de datos para persistir y recuperar datos
  - Ejecutar queries y operaciones CRUD sobre las entidades
  - Mapear resultados de la base de datos a entidades
- **Dependencias**: Ninguna (Spring Data JPA maneja la implementación)

### **4. Modelo de Dominio (ProductModel)**
- **Capa**: Dominio
- **Responsabilidades**:
  - Representar las entidades y conceptos del dominio de la aplicación (Product)
  - Contener la lógica de negocio que es intrínseca a la entidad en sí
  - Validar invariantes (reglas que siempre deben cumplirse)
  - Saber cómo construirse a partir de DTOs o Entidades, y cómo convertirse en Entidades o DTOs
- **Dependencias**: DTO, Entity

### **5. Entidad (ProductEntity)**
- **Responsabilidades**:
  - Representar la estructura de datos tal como está en la base de datos
  - Mapear las columnas de la tabla a campos de la clase con anotaciones JPA
- **Dependencias**: Ninguna

### **6. DTO (CreateProductDto, UpdateProductDto, ProductResponseDto...)**
- **Responsabilidades**:
  - Transferir datos entre el cliente y el servidor
  - Definir la estructura de datos que se recibe en las peticiones (Request DTO) o que se envía en las respuestas (Response DTO)
  - Contener validaciones de formato o integridad de datos con anotaciones de validación
- **Dependencias**: Ninguna

---

## **Flujo de una Petición**

Así fluye una petición a través de los componentes:

1. El **Controlador** recibe una petición HTTP (por ejemplo, POST /api/products)
2. El **Controlador** valida el DTO de entrada (CreateProductDto) con `@Valid`
3. Si el DTO es válido, el **Controlador** llama al método apropiado del **Servicio** (productService.create(dto))
4. El **Servicio** crea un **ProductModel** a partir del DTO (ProductModel.fromDto(dto))
5. El **Servicio** aplica la lógica de negocio o validaciones adicionales si es necesario
6. El **Servicio** convierte el **ProductModel** a una **Entidad** (product.toEntity())
7. El **Servicio** llama al **Repositorio** para guardar la **Entidad** (productRepository.save(entity))
8. El **Repositorio** guarda la **Entidad** en la base de datos y devuelve la **Entidad** guardada
9. El **Servicio** convierte la **Entidad** guardada de vuelta a un **ProductModel** (ProductModel.fromEntity(savedEntity))
10. El **Servicio** convierte el **ProductModel** a un DTO de respuesta (product.toResponseDto())
11. El **Servicio** devuelve el DTO de respuesta al **Controlador**
12. El **Controlador** devuelve el DTO de respuesta al cliente en la respuesta HTTP

---