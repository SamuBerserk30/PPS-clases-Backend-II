package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.InsufficientStockException;
import co.edu.cesde.pps.mapper.ProductMapper;
import co.edu.cesde.pps.model.Category;
import co.edu.cesde.pps.model.Product;
import co.edu.cesde.pps.repository.ProductRepository;
import co.edu.cesde.pps.util.CalculationUtils;
import co.edu.cesde.pps.util.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestión de productos.
 *
 * Responsabilidades:
 * - CRUD de productos
 * - Gestión de stock (verificar, actualizar, reservar)
 * - Validación de disponibilidad
 * - Búsqueda y filtrado
 * - Validación de SKU único
 * - Conversión Entity <-> DTO
 *
 * NOTA: En Etapa 06 se agregará:
 * - @Service annotation
 * - @Transactional
 * - Inyección de ProductRepository
 * - Persistencia real
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    public ProductService(CategoryService categoryService, ProductRepository productRepository) {
        this.productMapper = new ProductMapper();
        this.categoryService = categoryService;
        this.productRepository = productRepository;
    }

    /**
     * Crea un nuevo producto.
     *
     * @param productDTO Datos del producto
     * @return ProductDTO del producto creado
     * @throws DuplicateEntityException si el SKU ya existe
     * @throws EntityNotFoundException si la categoría no existe
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Validaciones
        ValidationUtils.validateNotBlank(productDTO.getSku(), "sku");
        ValidationUtils.validateNotBlank(productDTO.getName(), "name");
        ValidationUtils.validateNonNegative(productDTO.getPrice(), "price");
        ValidationUtils.validateNonNegative(BigDecimal.valueOf(productDTO.getStockQty()), "stockQty");

        // Verificar SKU único
        if (existsBySku(productDTO.getSku())) {
            throw new DuplicateEntityException("Product", "sku", productDTO.getSku());
        }

        // Obtener categoría
        Category category = categoryService.findCategoryEntityOrThrow(productDTO.getCategoryId());

        // Crear producto
        Product product = productMapper.toEntity(productDTO);
        product.setCategory(category);
        product.setImage(normalizeImage(productDTO.getImage()));
        product.setCreatedAt(LocalDateTime.now());
        category.getProducts().add(product);

        product = productRepository.save(product);

        return productMapper.toDTO(product);
    }

    /**
     * Actualiza un producto existente.
     *
     * @param productId ID del producto
     * @param productDTO Nuevos datos
     * @return ProductDTO actualizado
     * @throws EntityNotFoundException si no existe
     * @throws DuplicateEntityException si el nuevo SKU ya existe
     */
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product product = findProductEntityOrThrow(productId);

        // Validar SKU único si cambió
        if (!product.getSku().equals(productDTO.getSku()) && existsBySku(productDTO.getSku())) {
            throw new DuplicateEntityException("Product", "sku", productDTO.getSku());
        }

        // Validaciones
        ValidationUtils.validateNotBlank(productDTO.getName(), "name");
        ValidationUtils.validateNonNegative(productDTO.getPrice(), "price");
        ValidationUtils.validateNonNegative(BigDecimal.valueOf(productDTO.getStockQty()), "stockQty");

        // Actualizar campos
        product.setSku(productDTO.getSku());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setImage(normalizeImage(productDTO.getImage()));
        product.setPrice(productDTO.getPrice());
        product.setStockQty(productDTO.getStockQty());
        product.setIsActive(productDTO.getIsActive());

        // Actualizar categoría si cambió
        if (productDTO.getCategoryId() != null &&
                !productDTO.getCategoryId().equals(product.getCategory().getCategoryId())) {
            Category currentCategory = product.getCategory();
            Category newCategory = categoryService.findCategoryEntityOrThrow(productDTO.getCategoryId());
            currentCategory.getProducts().remove(product);
            newCategory.getProducts().add(product);
            product.setCategory(newCategory);
        }

        product = productRepository.save(product);

        return productMapper.toDTO(product);
    }

    /**
     * Elimina un producto (soft delete desactivándolo).
     *
     * @param productId ID del producto
     * @throws EntityNotFoundException si no existe
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProductEntityOrThrow(productId);
        product.setIsActive(false);
        productRepository.save(product);
    }

    /**
     * Busca producto por ID.
     *
     * @param productId ID del producto
     * @return ProductDTO
     * @throws EntityNotFoundException si no existe
     */
    public ProductDTO findById(Long productId) {
        Product product = findProductEntityOrThrow(productId);
        return productMapper.toDTO(product);
    }

    /**
     * Busca producto por SKU.
     *
     * @param sku SKU del producto
     * @return ProductDTO
     * @throws EntityNotFoundException si no existe
     */
    public ProductDTO findBySku(String sku) {
        Product product = productRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product with SKU: " + sku));

        return productMapper.toDTO(product);
    }

    /**
     * Lista todos los productos.
     *
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> findAllProducts() {
        return productMapper.toDTOList(productRepository.findAll());
    }

    /**
     * Lista productos activos.
     *
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> findActiveProducts() {
        return productMapper.toDTOList(productRepository.findByIsActiveTrue());
    }

    /**
     * Busca productos por categoría.
     *
     * @param categoryId ID de la categoría
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> findByCategory(Long categoryId) {
        categoryService.findCategoryEntityOrThrow(categoryId); // Validar que existe
        return productMapper.toDTOList(productRepository.findByCategory_CategoryId(categoryId));
    }

    /**
     * Busca productos por nombre (búsqueda parcial).
     *
     * @param name Nombre a buscar
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> searchByName(String name) {
        return productMapper.toDTOList(productRepository.findByNameContainingIgnoreCase(name));
    }

    /**
     * Verifica disponibilidad de producto con cantidad solicitada.
     *
     * @param productId ID del producto
     * @param quantity Cantidad solicitada
     * @return true si está disponible
     * @throws EntityNotFoundException si el producto no existe
     */
    public boolean checkAvailability(Long productId, Integer quantity) {
        Product product = findProductEntityOrThrow(productId);
        return product.getIsActive() &&
                CalculationUtils.hasEnoughStock(product.getStockQty(), quantity);
    }

    /**
     * Verifica si hay stock suficiente.
     *
     * @param productId ID del producto
     * @param quantity Cantidad requerida
     * @return true si hay stock suficiente
     * @throws EntityNotFoundException si el producto no existe
     */
    public boolean hasEnoughStock(Long productId, Integer quantity) {
        Product product = findProductEntityOrThrow(productId);
        return CalculationUtils.hasEnoughStock(product.getStockQty(), quantity);
    }

    /**
     * Actualiza el stock de un producto.
     *
     * @param productId ID del producto
     * @param newStock Nuevo stock
     * @throws EntityNotFoundException si el producto no existe
     */
    @Transactional
    public void updateStock(Long productId, Integer newStock) {
        Product product = findProductEntityOrThrow(productId);
        ValidationUtils.validateNonNegative(BigDecimal.valueOf(newStock), "stock");
        product.setStockQty(newStock);
        productRepository.save(product);
    }

    /**
     * Disminuye el stock de un producto (para ventas).
     *
     * @param productId ID del producto
     * @param quantity Cantidad a disminuir
     * @throws EntityNotFoundException si el producto no existe
     * @throws InsufficientStockException si no hay stock suficiente
     */
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = findProductEntityOrThrow(productId);

        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), quantity)) {
            throw new InsufficientStockException(productId, product.getSku(),
                    quantity, product.getStockQty());
        }

        int newStock = CalculationUtils.calculateNewStock(product.getStockQty(), quantity);
        product.setStockQty(newStock);
        productRepository.save(product);
    }

    /**
     * Aumenta el stock de un producto (para devoluciones o reposiciones).
     *
     * @param productId ID del producto
     * @param quantity Cantidad a aumentar
     * @throws EntityNotFoundException si el producto no existe
     */
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        Product product = findProductEntityOrThrow(productId);
        ValidationUtils.validatePositive(quantity, "quantity");

        int newStock = product.getStockQty() + quantity;
        product.setStockQty(newStock);
        productRepository.save(product);
    }

    /**
     * Verifica si existe un producto con el SKU dado.
     *
     * @param sku SKU a verificar
     * @return true si existe
     */
    public boolean existsBySku(String sku) {
        return productRepository.existsBySkuIgnoreCase(sku);
    }

    /**
     * Busca entity Product por ID o lanza excepción.
     * Método interno para uso de otros servicios.
     *
     * @param productId ID del producto
     * @return Product entity
     * @throws EntityNotFoundException si no existe
     */
    public Product findProductEntityOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
    }

    private String normalizeImage(String image) {
        if (image == null || image.isBlank()) {
            return null;
        }

        String normalizedImage = image.trim();
        ValidationUtils.validateMaxLength(normalizedImage, 1000, "image");
        return normalizedImage;
    }
}
