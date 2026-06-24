
# EVIDENCIA DEMOSTRATIVA

## 1 crear un producto nombre laptop
![crear un producto nombre laptop](images/revision/01-crear-laptop.png)

## 2 crear un producto nombre smartphone
![crear un producto nombre smartphone](images/revision/02-crear-smarthphone.png)

## 3 cambiar nombre del producto laptop a "notebook"
El id del producto creado laptop es 10. Por eso se usa esa ruta para hacer el PUT
![cambiar nombre del producto laptop a "notebook"](images/revision/03-cambiar-nombre.png)

## 4 eliminar el producto smartphone
![eliminar el producto smartphone](images/revision/04-aliminar-smarthphone.png)

5 en DB select a productos, debere ver los
2 productos
- notebook con fecha de modificación actual
- smartphone eliminado logico en true
![DB select a productos](images/revision/05-select-productos.png)

---

# Práctica 1: Spring Boot - Instalación, Configuración e Implementación del Primer Endpoint

**Asignatura:** Programación y Plataformas Web  
**Estudiante:** Verónica Cobos     

---
## 1. Captura de Verificación de Java

Salida del comando `java -version` verificando que Java 21 está instalado correctamente.

**Comando ejecutado:**
```bash
java -version
```

**Evidencia:**
![Verificación de Java](images/practica1/javaVerison.png)

---

## 2. Captura del Servidor Spring Boot Ejecutándose

Terminal mostrando que el servidor Tomcat se inició correctamente en el puerto 8080.

**Comando ejecutado:**
```bash
gradlew bootRun
```

**Evidencia:**
![Servidor Spring Boot ejecutándose](images/practica1/02_spring_boot_running.png)

---

## 3. Captura del Endpoint `/api/status` Funcionando

Navegador mostrando la respuesta JSON del endpoint REST implementado.

**URL accedida:**
```
http://localhost:8080/api/status
```

**Evidencia:**  
![Endpoint funcionando en navegador](images/practica1/03_endpoint_api_status.png)

---

## 4. Captura del Archivo StatusController.java Creado

Terminal mostrando que el archivo `StatusController.java` existe en la carpeta correcta.

**Comando ejecutado:**
```bash
ls ./src/main/java/ec/edu/ups/icc/fundamentos01/controllers/
```

**Evidencia:**
![Archivo StatusController.java creado](images/practica1/04_statuscontroller_exists.png)

---

## 5. Explicación del Estudiante

### 5.1 ¿Qué entendiste sobre el funcionamiento del endpoint?

El endpoint `/api/status` es una URL que responde a peticiones HTTP de tipo GET. Cuando accedo a `http://localhost:8080/api/status` desde mi navegador, el servidor Spring Boot procesa la solicitud y devuelve una respuesta en formato JSON. Esta respuesta contiene:

- `service`: El nombre del servicio ("Spring Boot API")
- `status`: El estado actual del servidor ("running")
- `timestamp`: La fecha y hora exacta en que se realizó la consulta

La anotación `@GetMapping("/api/status")` en el controlador especifica que este método solo responde a peticiones GET en esa ruta específica.

---

### 5.2 ¿Cuál es la función general de Spring Boot en la creación del servidor?

Spring Boot simplifica enormemente la creación de servidores web en Java. Sus principales funciones son:

1. **Servidor embebido:** Spring Boot incluye automáticamente un servidor Tomcat dentro de la aplicación. No necesito instalar Tomcat por separado ni configurarlo manualmente. Cuando ejecuto `gradlew bootRun`, Tomcat se inicia automáticamente en el puerto 8080.

2. **Auto-configuración:** Spring Boot detecta automáticamente las dependencias que agregué (Spring Web, Spring Boot DevTools) y configura todo lo necesario sin que tenga que escribir código de configuración complejo en XML.

3. **Ejecución rápida:** Puedo iniciar la aplicación con un simple comando y tener un servidor HTTP funcional en segundos, listo para recibir peticiones.

4. **Desarrollo ágil:** Spring Boot DevTools permite que los cambios en el código se reflejen automáticamente sin reiniciar el servidor, acelerando el desarrollo.

Sin Spring Boot, tendría que: instalar Tomcat, configurarlo manualmente, desplegar archivos `.war`, reiniciar servicios, etc. Spring Boot elimina toda esa complejidad.

---
# Práctica 2 (Spring Boot): Estructura del Proyecto, Arquitectura Interna y Organización Modular
---

## 1. Estructura modular en el IDE

En el IDE visual se ven las carpetas separadas (`config`, `utils`, `products`, `users`, `auth`) y dentro de `products` sus capas (`controllers`, `services`, etc.).

![Estructura modular del proyecto](images/practica2/01-estructura-modular.png)

---

## 2. Archivo Fundamentos01Application.java

Se debe verificar:

- el package raíz
- la ubicación correcta que permite ComponentScan

![Archivo Fundamentos01Application](images/practica2/02-fundamentos-application.png)

---
## 3. Explicación breve
### Por qué es importante tener módulos separados?


Tener los módulos separados es importante porque mantiene el proyecto ordenado y fácil de manejar cuando crece. Cada dominio (`products`, `users`, `auth`) tiene sus propias capas (controllers, services, repositories, etc.), así que todo lo de un mismo tema está junto y es más rápido de encontrar y mantener. Además, como cada módulo es bastante independiente, se puede trabajar en uno sin romper los demás, y el proyecto se parece más a una aplicación real en vez de tener todo amontonado en un solo lugar.

---
# Práctica 3 (Spring Boot): Construcción de una API REST usando controladores, DTOs, modelos y mappers

## 1. Consumo de los endpoints de Products desde Postman

### GET /api/products (con 3 productos creados)

Pedí la lista de productos y se ven los 3 que ya tengo creados.

![GET todos los products](images/practica3/01-get-products.png)

---

### GET /api/products/:id (producto existente)

Pedí un producto por su ID y me devuelve sus datos.

![GET product por id](images/practica3/02-get-product-id.png)

---

### DELETE /api/products/:id (producto existente)

Borré un producto que sí existe y me confirma que se eliminó.

![DELETE product existente](images/practica3/03-delete-product-ok.png)

---

### DELETE /api/products/:id (producto que no existe)

Intenté borrar un producto con un ID que no existe y me devuelve el mensaje de error.

![DELETE product inexistente](images/practica3/04-delete-product-notfound.png)

---
# Práctica 4 (Spring Boot): Controladores + Servicios + Lógica de Negocio

## 1. ProductServiceImpl.java completa

Aquí está la implementación del servicio con toda la lógica del CRUD. Se ve la anotación `@Service`, la lista en memoria, el generador de ID, el uso del mapper para convertir productos y los métodos.

![ProductServiceImpl.java](images/practica4/01-product-service-impl.png)

---

## 2. ProductsController.java

Aquí se ve el controlador que solo recibe peticiones y delega al servicio. Lo importante es que ya no tiene la lógica del CRUD, solo tiene la inyección del `ProductService` por constructor y cada endpoint simplemente llama al método correspondiente del servicio.

![ProductsController.java](images/practica4/02-products-controller.png)

---

## 3. Explicación breve
### ¿Cómo se inyecta el servicio en el controlador?
El servicio se inyecta en el controlador a través del constructor. Cuando Spring Boot ve que el controlador pide un `ProductService`, busca en todo el proyecto una clase que implemente esa interfaz. Encuentra `ProductServiceImpl` que tiene `@Service`, así que Spring automáticamente crea una instancia y la pasa al constructor del controlador. El controlador la guarda en una variable privada final y la usa. Así el controlador no tiene que crear el servicio manualmente, solo lo recibe listo para usar. Esto es la inyección de dependencias.

---
# Práctica 5 (Spring Boot): Persistencia real con PostgreSQL, Entidades JPA y Repositorios
## 1. Captura de 5 productos creados en PostgreSQL

Hice la consulta `SELECT * FROM products;` y aparecen los 5 productos que creé mediante la API REST. Se ven los campos id, name, price, stock, createdAt, updatedAt y deleted que vienen de BaseEntity.

![5 productos en PostgreSQL](images/practica5/01-productos-postgresql.png)

---

## 2. Explicación breve
### Explicar brevemente el flujo de datos desde la API REST hasta PostgreSQL y viceversa, destacando el uso de BaseEntity.

El flujo de datos funciona así: cuando se crea un producto desde la API (POST), el controlador recibe un CreateProductDto y lo pasa al servicio. El servicio convierte el DTO a ProductModel, luego a ProductEntity. Esa entidad se guarda en PostgreSQL mediante el repositorio. Cuando se consulta (GET), el repositorio trae la ProductEntity de la base de datos, se convierte a ProductModel, y luego a ProductResponseDto para devolver al cliente.

Lo importante de BaseEntity es que centraliza los campos comunes (id, createdAt, updatedAt, deleted) en una sola clase. Todas las entidades heredan de BaseEntity, así que no tengo que repetir esos campos en cada una. El `@PrePersist` asigna automáticamente la fecha de creación cuando se guarda por primera vez, y `@PreUpdate` actualiza la fecha de modificación cada vez que se edita. Esto permite rastrear cuándo se creó y cuándo se modificó cada registro.

---

## Notas Extra

- Versión de Java utilizada: **21**
- Versión de Spring Boot: **4.1.0**
- Build Tool: **Gradle**
- Servidor embebido: **Tomcat 11.0.x**
- Puerto del servidor: **8080**

---
