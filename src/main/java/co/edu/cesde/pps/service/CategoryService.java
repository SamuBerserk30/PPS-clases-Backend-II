package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.CategoryDTO;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.mapper.CategoryMapper;
import co.edu.cesde.pps.model.Category;
import co.edu.cesde.pps.util.StringUtils;
import co.edu.cesde.pps.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de categorías.
 *
 * Responsabilidades:
 * - CRUD de categorías
 * - Gestión de jerarquía (addSubcategory, removeSubcategory)
 * - Construcción de árbol de categorías
 * - Validación de slug único
 * - Validación de relaciones padre-hijo
 * - Conversión Entity <-> DTO
 *
 * NOTA: En Etapa 06 se agregará:
 * - @Service annotation
 * - @Transactional
 * - Inyección de CategoryRepository
 * - Persistencia real
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryMapper categoryMapper;
    // TODO Etapa 06: private final CategoryRepository categoryRepository;
    private final List<Category> categoriesInMemory;

    public CategoryService() {
        this.categoryMapper = new CategoryMapper();
        this.categoriesInMemory = new ArrayList<>();
    }

    /**
     * Crea una nueva categoría.
     *
     * @param categoryDTO Datos de la categoría
     * @return CategoryDTO de la categoría creada
     * @throws DuplicateEntityException si el slug ya existe
     */
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Validaciones
        ValidationUtils.validateNotBlank(categoryDTO.getName(), "name");

        // Generar slug si no existe
        String slug = categoryDTO.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = StringUtils.slugify(categoryDTO.getName());
        }

        // Verificar slug único
        if (existsBySlug(slug)) {
            throw new DuplicateEntityException("Category", "slug", slug);
        }

        // Crear categoría
        Category category = categoryMapper.toEntity(categoryDTO);
        category.setCategoryId(generateNextId());
        category.setSlug(slug);

        // Asignar parent si existe
        if (categoryDTO.getParentId() != null) {
            Category parent = findCategoryEntityOrThrow(categoryDTO.getParentId());
            category.setParent(parent);
        }

        // TODO Etapa 06: categoryRepository.save(category);
        categoriesInMemory.add(category);

        return categoryMapper.toDTO(category);
    }

    /**
     * Actualiza una categoría existente.
     *
     * @param categoryId ID de la categoría
     * @param categoryDTO Nuevos datos
     * @return CategoryDTO actualizado
     * @throws EntityNotFoundException si no existe
     * @throws DuplicateEntityException si el nuevo slug ya existe
     * @throws ValidationException si hay ciclo en jerarquía
     */
    @Transactional
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category category = findCategoryEntityOrThrow(categoryId);

        // Validaciones
        ValidationUtils.validateNotBlank(categoryDTO.getName(), "name");

        // Generar slug si cambió el nombre
        String newSlug = categoryDTO.getSlug();
        if (newSlug == null || newSlug.isBlank()) {
            newSlug = StringUtils.slugify(categoryDTO.getName());
        }

        // Verificar slug único si cambió
        if (!category.getSlug().equals(newSlug) && existsBySlug(newSlug)) {
            throw new DuplicateEntityException("Category", "slug", newSlug);
        }

        // Actualizar campos
        category.setName(categoryDTO.getName());
        category.setSlug(newSlug);

        // Actualizar parent si cambió
        if (categoryDTO.getParentId() != null) {
            // Validar que no sea su propio padre
            if (categoryDTO.getParentId().equals(categoryId)) {
                throw new ValidationException("Category cannot be its own parent");
            }

            Category newParent = findCategoryEntityOrThrow(categoryDTO.getParentId());

            // Validar que no cree ciclo
            if (wouldCreateCycle(category, newParent)) {
                throw new ValidationException("Cannot create cycle in category hierarchy");
            }

            category.setParent(newParent);
        } else {
            category.setParent(null); // Convertir en raíz
        }

        // TODO Etapa 06: categoryRepository.save(category);

        return categoryMapper.toDTO(category);
    }

    /**
     * Elimina una categoría.
     *
     * @param categoryId ID de la categoría
     * @throws EntityNotFoundException si no existe
     * @throws ValidationException si tiene subcategorías o productos
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = findCategoryEntityOrThrow(categoryId);

        // Validar que no tenga subcategorías
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            throw new ValidationException("Cannot delete category with subcategories");
        }

        // Validar que no tenga productos
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new ValidationException("Cannot delete category with products");
        }

        // TODO Etapa 06: categoryRepository.delete(category);
        categoriesInMemory.remove(category);
    }

    /**
     * Busca categoría por ID.
     *
     * @param categoryId ID de la categoría
     * @return CategoryDTO
     * @throws EntityNotFoundException si no existe
     */
    public CategoryDTO findById(Long categoryId) {
        Category category = findCategoryEntityOrThrow(categoryId);
        return categoryMapper.toDTO(category);
    }

    /**
     * Busca categoría por slug.
     *
     * @param slug Slug de la categoría
     * @return CategoryDTO
     * @throws EntityNotFoundException si no existe
     */
    public CategoryDTO findBySlug(String slug) {
        // TODO Etapa 06: Category category = categoryRepository.findBySlug(slug)
        Category category = categoriesInMemory.stream()
                .filter(c -> c.getSlug().equalsIgnoreCase(slug))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Category with slug: " + slug));

        return categoryMapper.toDTO(category);
    }

    /**
     * Lista todas las categorías.
     *
     * @return Lista de CategoryDTO
     */
    public List<CategoryDTO> findAllCategories() {
        // TODO Etapa 06: List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDTOList(categoriesInMemory);
    }

    /**
     * Lista categorías raíz (sin padre).
     *
     * @return Lista de CategoryDTO
     */
    public List<CategoryDTO> findRootCategories() {
        // TODO Etapa 06: List<Category> roots = categoryRepository.findByParentIsNull();
        List<Category> rootCategories = categoriesInMemory.stream()
                .filter(Category::isRootCategory)
                .collect(Collectors.toList());

        return categoryMapper.toDTOList(rootCategories);
    }

    /**
     * Lista subcategorías de una categoría.
     *
     * @param parentId ID de la categoría padre
     * @return Lista de CategoryDTO
     */
    public List<CategoryDTO> findSubcategories(Long parentId) {
        Category parent = findCategoryEntityOrThrow(parentId);

        // TODO Etapa 06: List<Category> subs = categoryRepository.findByParentId(parentId);
        List<Category> subcategories = categoriesInMemory.stream()
                .filter(c -> c.getParent() != null &&
                           c.getParent().getCategoryId().equals(parentId))
                .collect(Collectors.toList());

        return categoryMapper.toDTOList(subcategories);
    }

    /**
     * Agrega una subcategoría a una categoría (gestión bidireccional).
     *
     * @param parentId ID de la categoría padre
     * @param subcategoryDTO Datos de la subcategoría
     * @return CategoryDTO de la subcategoría creada
     * @throws EntityNotFoundException si el padre no existe
     * @throws DuplicateEntityException si el slug ya existe
     */
    @Transactional
    public CategoryDTO addSubcategory(Long parentId, CategoryDTO subcategoryDTO) {
        Category parent = findCategoryEntityOrThrow(parentId);

        // Validaciones
        ValidationUtils.validateNotBlank(subcategoryDTO.getName(), "name");

        // Generar slug
        String slug = subcategoryDTO.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = StringUtils.slugify(subcategoryDTO.getName());
        }

        if (existsBySlug(slug)) {
            throw new DuplicateEntityException("Category", "slug", slug);
        }

        // Crear subcategoría
        Category subcategory = categoryMapper.toEntity(subcategoryDTO);
        subcategory.setCategoryId(generateNextId());
        subcategory.setSlug(slug);

        // Gestión bidireccional
        parent.getSubcategories().add(subcategory);  // Agregar a colección
        subcategory.setParent(parent);                // Establecer referencia

        // TODO Etapa 06: categoryRepository.save(subcategory);
        categoriesInMemory.add(subcategory);

        return categoryMapper.toDTO(subcategory);
    }

    /**
     * Remueve una subcategoría de su padre (gestión bidireccional).
     *
     * @param parentId ID de la categoría padre
     * @param subcategoryId ID de la subcategoría
     * @throws EntityNotFoundException si no existen
     * @throws ValidationException si la subcategoría no pertenece al padre
     */
    @Transactional
    public void removeSubcategory(Long parentId, Long subcategoryId) {
        Category parent = findCategoryEntityOrThrow(parentId);
        Category subcategory = findCategoryEntityOrThrow(subcategoryId);

        // Validar que la subcategoría pertenezca al padre
        if (subcategory.getParent() == null ||
            !subcategory.getParent().getCategoryId().equals(parentId)) {
            throw new ValidationException("Category is not a subcategory of specified parent");
        }

        // Gestión bidireccional
        parent.getSubcategories().remove(subcategory);  // Remover de colección
        subcategory.setParent(null);                     // Remover referencia (convertir en raíz)

        // TODO Etapa 06: categoryRepository.save(subcategory);
    }

    /**
     * Construye árbol de categorías completo desde una categoría raíz.
     *
     * @param categoryId ID de la categoría raíz
     * @return CategoryDTO con subcategorías anidadas
     * @throws EntityNotFoundException si no existe
     */
    public CategoryDTO buildCategoryTree(Long categoryId) {
        Category category = findCategoryEntityOrThrow(categoryId);
        return categoryMapper.toDTOWithHierarchy(category);
    }

    /**
     * Construye árbol completo de todas las categorías raíz.
     *
     * @return Lista de CategoryDTO con jerarquías completas
     */
    public List<CategoryDTO> buildFullCategoryTree() {
        List<Category> rootCategories = categoriesInMemory.stream()
                .filter(Category::isRootCategory)
                .collect(Collectors.toList());

        return categoryMapper.toDTOListWithHierarchy(rootCategories);
    }

    /**
     * Verifica si existe una categoría con el slug dado.
     *
     * @param slug Slug a verificar
     * @return true si existe
     */
    public boolean existsBySlug(String slug) {
        // TODO Etapa 06: return categoryRepository.existsBySlug(slug);
        return categoriesInMemory.stream()
                .anyMatch(c -> c.getSlug().equalsIgnoreCase(slug));
    }

    /**
     * Busca entity Category por ID o lanza excepción.
     * Método interno para uso de otros servicios.
     *
     * @param categoryId ID de la categoría
     * @return Category entity
     * @throws EntityNotFoundException si no existe
     */
    public Category findCategoryEntityOrThrow(Long categoryId) {
        // TODO Etapa 06: return categoryRepository.findById(categoryId)
        return categoriesInMemory.stream()
                .filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Category", categoryId));
    }

    // Métodos privados auxiliares

    /**
     * Verifica si asignar newParent a category crearía un ciclo.
     */
    private boolean wouldCreateCycle(Category category, Category newParent) {
        Category current = newParent;
        while (current != null) {
            if (current.getCategoryId().equals(category.getCategoryId())) {
                return true; // Ciclo detectado
            }
            current = current.getParent();
        }
        return false;
    }

    // Método auxiliar para simular auto-increment
    private Long generateNextId() {
        return categoriesInMemory.stream()
                .mapToLong(Category::getCategoryId)
                .max()
                .orElse(0L) + 1;
    }
}
