package co.edu.cesde.pps.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad Category - Organiza el catálogo en categorías jerárquicas.
 *
 * Soporta estructura tipo árbol mediante auto-referencia (parent).
 * Ejemplo: "Computadores" → "Portátiles" → "Gaming"
 *
 * Campos:
 * - categoryId: Identificador único de la categoría (PK)
 * - parent: Categoría padre (N:1 con Category) - NULLABLE para categorías raíz
 * - name: Nombre de la categoría
 * - slug: URL-friendly identifier (UNIQUE) - para URLs amigables
 * - subcategories: Lista de subcategorías (1:N con Category)
 * - products: Lista de productos de esta categoría (1:N con Product)
 *
 * Relaciones:
 * - N:1 con Category (auto-referencia para jerarquía - muchas categorías tienen un padre)
 * - 1:N con Category (una categoría tiene muchas subcategorías)
 * - 1:N con Product (una categoría tiene muchos productos)
 *
 * NOTA: Los métodos de gestión bidireccional (addSubcategory, removeSubcategory) fueron movidos
 * a la capa de servicio (CategoryService) en etapa 05 para mantener el modelo limpio.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Category {

    private Long categoryId;
    private Category parent; // Nullable - NULL para categorías raíz
    private String name;
    private String slug;

    // Colecciones para relaciones 1:N
    private List<Category> subcategories;
    private List<Product> products;

    // Constructor vacío (requerido para JPA futuro)
    public Category() {
        this.subcategories = new ArrayList<>();
        this.products = new ArrayList<>();
    }

    // Constructor para categoría raíz (sin parent)
    public Category(String name, String slug) {
        this.parent = null; // Categoría raíz
        this.name = name;
        this.slug = slug;
        this.subcategories = new ArrayList<>();
        this.products = new ArrayList<>();
    }

    // Constructor para subcategoría (con parent)
    public Category(Category parent, String name, String slug) {
        this.parent = parent;
        this.name = name;
        this.slug = slug;
        this.subcategories = new ArrayList<>();
        this.products = new ArrayList<>();
    }

    // Getters y Setters

    // Métodos helper de consulta (sin efectos secundarios)

    /**
     * Verifica si es categoría raíz
     */
    public boolean isRootCategory() {
        return parent == null;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryId, category.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    // toString sin navegación a objetos relacionados (solo IDs y tamaño de colecciones)

}
