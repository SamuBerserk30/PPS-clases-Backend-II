package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.CatalogApplicationService;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.web.dto.response.ProductResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.PRODUCTS)
public class ProductController {

    private final CatalogApplicationService catalogApplicationService;

    public ProductController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @GetMapping
    public List<ProductResponse> listProducts(@RequestParam(required = false) String search,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<ProductResponse> products = resolveBaseProducts(search, categoryId);

        return products.stream()
                .filter(product -> categoryId == null || categoryId.equals(product.categoryId()))
                .filter(product -> !activeOnly || Boolean.TRUE.equals(product.isActive()))
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        ProductResponse response = catalogApplicationService.getProduct(id);
        if (!Boolean.TRUE.equals(response.isActive())) {
            throw new EntityNotFoundException("Product", id);
        }
        return response;
    }

    private List<ProductResponse> resolveBaseProducts(String search, Long categoryId) {
        if (search != null && !search.isBlank()) {
            return catalogApplicationService.searchProducts(search);
        }
        if (categoryId != null) {
            return catalogApplicationService.listProductsByCategory(categoryId);
        }
        return catalogApplicationService.listProducts(false);
    }
}

