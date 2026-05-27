package co.edu.cesde.pps.application;

import co.edu.cesde.pps.dto.CategoryDTO;
import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.service.CategoryService;
import co.edu.cesde.pps.service.ProductService;
import co.edu.cesde.pps.web.dto.request.CategoryUpsertRequest;
import co.edu.cesde.pps.web.dto.request.ProductUpsertRequest;
import co.edu.cesde.pps.web.dto.response.CategoryResponse;
import co.edu.cesde.pps.web.dto.response.ProductResponse;
import co.edu.cesde.pps.web.mapper.WebRequestMapper;
import co.edu.cesde.pps.web.mapper.WebResponseMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de catálogo y administración básica del mismo.
 */
@Service
@Transactional(readOnly = true)
public class CatalogApplicationService {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final WebRequestMapper webRequestMapper;
    private final WebResponseMapper webResponseMapper;

    public CatalogApplicationService(CategoryService categoryService,
                                     ProductService productService,
                                     WebRequestMapper webRequestMapper,
                                     WebResponseMapper webResponseMapper) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.webRequestMapper = webRequestMapper;
        this.webResponseMapper = webResponseMapper;
    }

    public List<CategoryResponse> listCategories() {
        return webResponseMapper.toCategoryResponseList(categoryService.findAllCategories());
    }

    public List<CategoryResponse> listCategoryTree() {
        return webResponseMapper.toCategoryResponseList(categoryService.buildFullCategoryTree());
    }

    public CategoryResponse getCategory(Long categoryId) {
        return webResponseMapper.toCategoryResponse(categoryService.findById(categoryId));
    }

    public List<CategoryResponse> listSubcategories(Long parentId) {
        return webResponseMapper.toCategoryResponseList(categoryService.findSubcategories(parentId));
    }

    public List<ProductResponse> listProducts(boolean activeOnly) {
        return webResponseMapper.toProductResponseList(
                activeOnly ? productService.findActiveProducts() : productService.findAllProducts()
        );
    }

    public ProductResponse getProduct(Long productId) {
        return webResponseMapper.toProductResponse(productService.findById(productId));
    }

    public List<ProductResponse> searchProducts(String search) {
        return webResponseMapper.toProductResponseList(productService.searchByName(search));
    }

    public List<ProductResponse> listProductsByCategory(Long categoryId) {
        return webResponseMapper.toProductResponseList(productService.findByCategory(categoryId));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryUpsertRequest request) {
        CategoryDTO dto = webRequestMapper.toCategoryDTO(request);
        return webResponseMapper.toCategoryResponse(categoryService.createCategory(dto));
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryUpsertRequest request) {
        CategoryDTO dto = webRequestMapper.toCategoryDTO(request);
        return webResponseMapper.toCategoryResponse(categoryService.updateCategory(categoryId, dto));
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }

    @Transactional
    public ProductResponse createProduct(ProductUpsertRequest request) {
        ProductDTO dto = webRequestMapper.toProductDTO(request);
        return webResponseMapper.toProductResponse(productService.createProduct(dto));
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpsertRequest request) {
        ProductDTO dto = webRequestMapper.toProductDTO(request);
        return webResponseMapper.toProductResponse(productService.updateProduct(productId, dto));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        productService.deleteProduct(productId);
    }
}

