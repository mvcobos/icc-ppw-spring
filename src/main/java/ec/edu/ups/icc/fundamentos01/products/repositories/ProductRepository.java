package ec.edu.ups.icc.fundamentos01.products.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;

/*
 * Repositorio encargado de gestionar la persistencia
 * de productos usando Spring Data JPA.
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    // Hereda automáticamente todos los métodos CRUD (save, findById, findAll, deleteById)

    // "categories" es una relación many-to-many, por eso se atraviesa como colección: findByCategoriesId
    List<ProductEntity> findByCategoriesIdAndIsDeletedFalse(Long categoryId);

    List<ProductEntity> findByOwnerIdAndIsDeletedFalse(Long userId);

    /*
     * Busca productos activos de un usuario aplicando filtros opcionales.
     *
     * Si un filtro llega como null, no se aplica.
     */
    @Query("""
            SELECT DISTINCT p
            FROM ProductEntity p
            LEFT JOIN p.categories c
            WHERE p.isDeleted = false
              AND p.owner.id = :userId
              AND p.owner.isDeleted = false
              AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:categoryId IS NULL OR (c.id = :categoryId AND c.isDeleted = false))
            """)
    List<ProductEntity> findByOwnerIdWithFilters(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId
    );

    /*
     * Busca productos activos de una categoría aplicando filtros opcionales.
     *
     * Si un filtro llega como null, no se aplica.
     */
    @Query("""
            SELECT DISTINCT p
            FROM ProductEntity p
            JOIN p.categories c
            WHERE p.isDeleted = false
              AND c.id = :categoryId
              AND c.isDeleted = false
              AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:userId IS NULL OR (p.owner.id = :userId AND p.owner.isDeleted = false))
            """)
    List<ProductEntity> findByCategoryIdWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("userId") Long userId
    );

    /*
     * Consulta productos activos usando Page.
     *
     * Page ejecuta consulta de datos y consulta COUNT.
     */
    @Query(
            value = """
                    SELECT p
                    FROM ProductEntity p
                    WHERE p.isDeleted = false
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM ProductEntity p
                    WHERE p.isDeleted = false
                    """
    )
    Page<ProductEntity> findActivePage(Pageable pageable);

    /*
     * Consulta productos activos usando Slice.
     *
     * Slice no necesita total de registros.
     */
    @Query("""
            SELECT p
            FROM ProductEntity p
            WHERE p.isDeleted = false
            """)
    Slice<ProductEntity> findActiveSlice(Pageable pageable);

    /*
     * Consulta productos activos de un usuario usando Slice.
     *
     * Se usa para que cada usuario autenticado vea únicamente
     * sus propios productos, filtrando desde la consulta y no en memoria.
     */
    @Query("""
            SELECT p
            FROM ProductEntity p
            WHERE p.isDeleted = false
              AND p.owner.id = :ownerId
            """)
    Slice<ProductEntity> findActiveSliceByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    /*
     * Consulta paginada (Page) de productos activos de una categoría,
     * aplicando los mismos filtros opcionales que findByCategoryIdWithFilters.
     */
    @Query(
            value = """
                    SELECT DISTINCT p
                    FROM ProductEntity p
                    JOIN p.categories c
                    WHERE p.isDeleted = false
                      AND c.id = :categoryId
                      AND c.isDeleted = false
                      AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
                      AND (:minPrice IS NULL OR p.price >= :minPrice)
                      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                      AND (:userId IS NULL OR (p.owner.id = :userId AND p.owner.isDeleted = false))
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT p)
                    FROM ProductEntity p
                    JOIN p.categories c
                    WHERE p.isDeleted = false
                      AND c.id = :categoryId
                      AND c.isDeleted = false
                      AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
                      AND (:minPrice IS NULL OR p.price >= :minPrice)
                      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                      AND (:userId IS NULL OR (p.owner.id = :userId AND p.owner.isDeleted = false))
                    """
    )
    Page<ProductEntity> findByCategoryIdWithFiltersPage(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("userId") Long userId,
            Pageable pageable
    );

    /*
     * Consulta paginada (Slice) de productos activos de una categoría,
     * aplicando los mismos filtros opcionales que findByCategoryIdWithFilters.
     */
    @Query("""
            SELECT DISTINCT p
            FROM ProductEntity p
            JOIN p.categories c
            WHERE p.isDeleted = false
              AND c.id = :categoryId
              AND c.isDeleted = false
              AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:userId IS NULL OR (p.owner.id = :userId AND p.owner.isDeleted = false))
            """)
    Slice<ProductEntity> findByCategoryIdWithFiltersSlice(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("userId") Long userId,
            Pageable pageable
    );
}
