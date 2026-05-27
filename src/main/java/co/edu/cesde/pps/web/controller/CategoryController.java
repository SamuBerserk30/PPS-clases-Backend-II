package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.CatalogApplicationService;
import co.edu.cesde.pps.web.dto.response.CategoryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.CATEGORIES)
public class CategoryController {

    private final CatalogApplicationService catalogApplicationService;

    public CategoryController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @GetMapping
    public List<CategoryResponse> listCategories() {
        return catalogApplicationService.listCategories();
    }

    @GetMapping("/tree")
    public List<CategoryResponse> listCategoryTree() {
        return catalogApplicationService.listCategoryTree();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        return catalogApplicationService.getCategory(id);
    }

    @GetMapping("/{id}/subcategories")
    public List<CategoryResponse> listSubcategories(@PathVariable Long id) {
        return catalogApplicationService.listSubcategories(id);
    }
}

