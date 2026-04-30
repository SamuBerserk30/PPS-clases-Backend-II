package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.InsufficientStockException;
import co.edu.cesde.pps.mapper.ProductMapper;
import co.edu.cesde.pps.model.Category;
import co.edu.cesde.pps.model.Product;
import co.edu.cesde.pps.util.CalculationUtils;
import co.edu.cesde.pps.util.ValidationUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    // TODO Etapa 06: private final ProductRepository productRepository;
    private final List<Product> productsInMemory;

    public ProductService(CategoryService categoryService) {
        this.productMapper = new ProductMapper();
        this.categoryService = categoryService;
        this.productsInMemory = new ArrayList<>();
    }

    /**
     * Crea un nuevo producto.
     *
     * @param productDTO Datos del producto
     * @return ProductDTO del producto creado
     * @throws DuplicateEntityException si el SKU ya existe
     * @throws EntityNotFoundException si la categoría no existe
     */
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
        product.setProductId(generateNextId());
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());

        // TODO Etapa 06: productRepository.save(product);
        productsInMemory.add(product);

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
        product.setPrice(productDTO.getPrice());
        product.setStockQty(productDTO.getStockQty());
        product.setIsActive(productDTO.getIsActive());

        // Actualizar categoría si cambió
        if (productDTO.getCategoryId() != null &&
            !productDTO.getCategoryId().equals(product.getCategory().getCategoryId())) {
            Category newCategory = categoryService.findCategoryEntityOrThrow(productDTO.getCategoryId());
            product.setCategory(newCategory);
        }

        // TODO Etapa 06: productRepository.save(product);

        return productMapper.toDTO(product);
    }

    /**
     * Elimina un producto (soft delete desactivándolo).
     *
     * @param productId ID del producto
     * @throws EntityNotFoundException si no existe
     */
    public void deleteProduct(Long productId) {
        Product product = findProductEntityOrThrow(productId);
        product.setIsActive(false);
        // TODO Etapa 06: productRepository.save(product);
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
        // TODO Etapa 06: Product product = productRepository.findBySku(sku)
        Product product = productsInMemory.stream()
                .filter(p -> p.getSku().equalsIgnoreCase(sku))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Product with SKU: " + sku));

        return productMapper.toDTO(product);
    }

    /**
     * Lista todos los productos.
     *
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> findAllProducts() {
        // TODO Etapa 06: List<Product> products = productRepository.findAll();
        return productMapper.toDTOList(productsInMemory);
    }

    /**
     * Lista productos activos.
     *
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> findActiveProducts() {
        // TODO Etapa 06: List<Product> products = productRepository.findByIsActive(true);
        List<Product> activeProducts = productsInMemory.stream()
                .filter(Product::getIsActive)
                .collect(Collectors.toList());

        return productMapper.toDTOList(activeProducts);
    }

    /**
     * Busca productos por categoría.
     *
     * @param categoryId ID de la categoría
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> findByCategory(Long categoryId) {
        categoryService.findCategoryEntityOrThrow(categoryId); // Validar que existe

        // TODO Etapa 06: List<Product> products = productRepository.findByCategoryId(categoryId);
        List<Product> categoryProducts = productsInMemory.stream()
                .filter(p -> p.getCategory().getCategoryId().equals(categoryId))
                .collect(Collectors.toList());

        return productMapper.toDTOList(categoryProducts);
    }

    /**
     * Busca productos por nombre (búsqueda parcial).
     *
     * @param name Nombre a buscar
     * @return Lista de ProductDTO
     */
    public List<ProductDTO> searchByName(String name) {
        // TODO Etapa 06: List<Product> products = productRepository.findByNameContaining(name);
        List<Product> matchingProducts = productsInMemory.stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        return productMapper.toDTOList(matchingProducts);
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
    public void updateStock(Long productId, Integer newStock) {
        Product product = findProductEntityOrThrow(productId);
        ValidationUtils.validateNonNegative(BigDecimal.valueOf(newStock), "stock");
        product.setStockQty(newStock);
        // TODO Etapa 06: productRepository.save(product);
    }

    /**
     * Disminuye el stock de un producto (para ventas).
     *
     * @param productId ID del producto
     * @param quantity Cantidad a disminuir
     * @throws EntityNotFoundException si el producto no existe
     * @throws InsufficientStockException si no hay stock suficiente
     */
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = findProductEntityOrThrow(productId);

        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), quantity)) {
            throw new InsufficientStockException(productId, product.getSku(),
                quantity, product.getStockQty());
        }

        int newStock = CalculationUtils.calculateNewStock(product.getStockQty(), quantity);
        product.setStockQty(newStock);
        // TODO Etapa 06: productRepository.save(product);
    }

    /**
     * Aumenta el stock de un producto (para devoluciones o reposiciones).
     *
     * @param productId ID del producto
     * @param quantity Cantidad a aumentar
     * @throws EntityNotFoundException si el producto no existe
     */
    public void increaseStock(Long productId, Integer quantity) {
        Product product = findProductEntityOrThrow(productId);
        ValidationUtils.validatePositive(quantity, "quantity");

        int newStock = product.getStockQty() + quantity;
        product.setStockQty(newStock);
        // TODO Etapa 06: productRepository.save(product);
    }

    /**
     * Verifica si existe un producto con el SKU dado.
     *
     * @param sku SKU a verificar
     * @return true si existe
     */
    public boolean existsBySku(String sku) {
        // TODO Etapa 06: return productRepository.existsBySku(sku);
        return productsInMemory.stream()
                .anyMatch(p -> p.getSku().equalsIgnoreCase(sku));
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
        // TODO Etapa 06: return productRepository.findById(productId)
        return productsInMemory.stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
    }

    // Método auxiliar para simular auto-increment
    private Long generateNextId() {
        return productsInMemory.stream()
                .mapToLong(Product::getProductId)
                .max()
                .orElse(0L) + 1;
    }
}
