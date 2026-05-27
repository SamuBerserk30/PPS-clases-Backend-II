package co.edu.cesde.pps.web.dto.response;

import java.util.List;

public record CategoryResponse(
        Long id,
        Long parentId,
        String parentName,
        String name,
        String slug,
        Boolean isRoot,
        Integer subcategoriesCount,
        Integer productsCount,
        List<CategoryResponse> subcategories
) {
}

