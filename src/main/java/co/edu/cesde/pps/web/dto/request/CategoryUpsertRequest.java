package co.edu.cesde.pps.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpsertRequest(
        Long parentId,
        @NotBlank
        String name,
        String slug
) {
}

