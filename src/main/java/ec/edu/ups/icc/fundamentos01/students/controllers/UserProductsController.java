package ec.edu.ups.icc.fundamentos01.students.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByUserDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import jakarta.validation.Valid;

/*
 * Controlador REST encargado de exponer consultas relacionadas
 * entre usuarios y productos.
 *
 * La ruta pertenece al contexto semántico de usuarios:
 * /users/{id}/products
 *
 * Sin embargo, la lógica se delega a ProductService
 * porque el recurso consultado es products.
 */
@RestController
@RequestMapping("/users")
public class UserProductsController {

    private final ProductService productService;

    public UserProductsController(

            ProductService productService
    ) {
        this.productService = productService;
    }


    /*
     * Endpoint para consultar productos de un usuario.
     *
     * GET /api/users/{id}/products
     * GET /api/users/{id}/products?name=laptop
     * GET /api/users/{id}/products?minPrice=500&maxPrice=1500
     * GET /api/users/{id}/products?categoryId=2
     */
    @GetMapping("/{id}/products")
    public List<ProductResponseDto> findProductsByUser(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductFilterByUserDto filters
    ) {
        return productService.findByUserIdWithFilters(id, filters);
    }
}
