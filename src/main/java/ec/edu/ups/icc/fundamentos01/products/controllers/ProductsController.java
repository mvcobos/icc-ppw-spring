package ec.edu.ups.icc.fundamentos01.products.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/*
 * Controlador REST encargado de exponer los endpoints HTTP
 * para la gestión de productos.
 *
 * En esta práctica el controlador ya no contiene la lógica del CRUD.
 * Solo recibe la petición y delega la operación al servicio.
 */

@Tag(
    name = "Productos",
    description = "Gestión de productos: creación, actualización, eliminación y consulta"
)
@SecurityRequirement(name = "bearerAuth") // Requiere token JWT
@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    /*
     * Endpoint para listar todos los productos.
     * Se mantiene sin paginación para comparar con los endpoints paginados.
     *
     * Solo ADMIN puede acceder, ya que expone información de todos los usuarios.
     * GET /products
     */
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponseDto> findAll() {
        return productService.findAll();
    }

    /*
     * Endpoint paginado usando Page.
     *
     * GET /products/page
     * GET /products/page?page=0&size=5
     * GET /products/page?page=0&size=5&sortBy=price&direction=desc
     */
    @Operation(
        summary = "Listar productos paginados",
        description = "Devuelve una página de productos según parámetros de paginación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Página de productos devuelta exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de paginación inválidos")
    })
    @GetMapping("/page")
    public Page<ProductResponseDto> findAllPage(
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return productService.findAllPage(pagination);
    }

    /*
     * Endpoint paginado usando Slice.
     *
     * Solo devuelve los productos del usuario autenticado.
     *
     * GET /products/slice
     * GET /products/slice?page=0&size=5
     * GET /products/slice?page=0&size=5&sortBy=createdAt&direction=desc
     */
    @Operation(
        summary = "Listar productos en una slice",
        description = "Devuelve una slice de productos según parámetros de paginación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Slice de productos devuelta exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de paginación inválidos"
        )
    })
    @GetMapping("/slice")
    public Slice<ProductResponseDto> findAllSlice(
            @Valid @ModelAttribute PaginationDto pagination,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return productService.findAllSlice(pagination, currentUser);
    }

    /*
     * Endpoint para buscar un producto por id.
     * GET /products/{id}
     */
    @Operation(
        summary = "Buscar producto por ID",
        description = "Devuelve un producto según su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado"
        )
    })
    @GetMapping("/{id}")
    public Object findOne(@PathVariable("id")  Long id) {
        return productService.findOne(id);
    }

    /*
     * Endpoint para listar los productos de una categoría.
     * GET /products/category/{id}
     */
    @Operation(
        summary = "Listar productos por categoría",
        description = "Devuelve una lista de productos según su categoría."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Productos encontrados exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada"
        )
    })
    @GetMapping("/category/{id}")
    public List<ProductResponseDto> findByCategory(@PathVariable("id") Long id) {
        return productService.findByCategory(id);
    }

    /*
     * Endpoint para listar los productos de una categoría.
     * GET /products/user/{id}
     */
    @Operation(
        summary = "Listar productos por usuario",
        description = "Devuelve una lista de productos según su propietario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Productos encontrados exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    @GetMapping("/user/{id}")
    public List<ProductResponseDto> findByUser(@PathVariable("id") Long id) {
        return productService.findByUser(id);
    }

    /*
     * Endpoint para crear un nuevo producto.
     * POST /products
     *
     * El owner ya no se toma desde el body: se obtiene desde el token JWT
     * mediante @AuthenticationPrincipal, para que nadie pueda crear
     * productos a nombre de otro usuario.
     */
    @Operation(
        summary = "Crear nuevo producto",
        description = "Crea un nuevo producto con los datos proporcionados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos del producto inválidos"
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(
            @Valid @RequestBody CreateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return productService.create(dto, currentUser);
    }

    /*
     * Endpoint para actualizar completamente un producto.
     * PUT /products/{id}
     *
     * La validación de ownership (solo el dueño o un ADMIN pueden
     * modificarlo) se hace dentro del servicio.
     */
    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza completamente un producto con los datos proporcionados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto actualizado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos del producto inválidos"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado"
        )
    })
    @PutMapping("/{id}")
    public Object update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return productService.update(id, dto, currentUser);
    }

    /*
     * Endpoint para actualizar parcialmente un producto.
     * PATCH /products/{id}
     */
    @Operation(
        summary = "Actualizar producto parcialmente",
        description = "Actualiza parcialmente un producto con los datos proporcionados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto actualizado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos del producto inválidos"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado"
        )
    })
    @PatchMapping("/{id}")
    public Object partialUpdate(
            @PathVariable("id") Long id,
            @Valid @RequestBody PartialUpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return productService.partialUpdate(id, dto, currentUser);
    }

    /*
     * Endpoint para eliminar un producto.
     * DELETE /products/{id}
     *
     * La validación de ownership se hace dentro del servicio.
     */
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto según su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Producto eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado"
        )
    })
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        productService.delete(id, currentUser);
    }
}