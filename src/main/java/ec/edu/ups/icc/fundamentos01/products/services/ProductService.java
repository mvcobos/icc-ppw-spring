package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByCategoryDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByUserDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;

/*
 * Servicio que define las operaciones disponibles
 * para la gestión de productos.
 */
public interface ProductService {
    List<ProductResponseDto> findAll();
    Object findOne(Long id);
    List<ProductResponseDto> findByCategory(Long categoryId);
    List<ProductResponseDto> findByUser(Long userId);

    /*
     * Crea un producto usando como owner al usuario autenticado.
     */
    ProductResponseDto create(CreateProductDto dto, UserDetailsImpl currentUser);

    /*
     * Actualiza completamente un producto.
     * Se valida ownership en el servicio.
     */
    Object update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser);

    /*
     * Actualiza parcialmente un producto.
     * Se valida ownership en el servicio.
     */
    Object partialUpdate(Long id, PartialUpdateProductDto dto, UserDetailsImpl currentUser);

    /*
     * Elimina lógicamente un producto.
     * Se valida ownership en el servicio.
     */
    void delete(Long id, UserDetailsImpl currentUser);

    List<ProductResponseDto> findByUserIdWithFilters(
            Long userId,
            ProductFilterByUserDto filters
    );

    List<ProductResponseDto> findByCategoryIdWithFilters(
            Long categoryId,
            ProductFilterByCategoryDto filters
    );

    /*
     * Retorna productos activos usando Page.
     */
    Page<ProductResponseDto> findAllPage(PaginationDto pagination);

    /*
     * Retorna, usando Slice, únicamente los productos activos
     * que pertenecen al usuario autenticado.
     */
    Slice<ProductResponseDto> findAllSlice(PaginationDto pagination, UserDetailsImpl currentUser);

    /*
     * Retorna productos de una categoría con filtros y Page.
     */
    Page<ProductResponseDto> findByCategoryIdWithFiltersPage(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    );

    /*
     * Retorna productos de una categoría con filtros y Slice.
     */
    Slice<ProductResponseDto> findByCategoryIdWithFiltersSlice(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    );
}
