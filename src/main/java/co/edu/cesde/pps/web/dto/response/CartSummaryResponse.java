package co.edu.cesde.pps.web.dto.response;

import java.math.BigDecimal;

public record CartSummaryResponse(
        Integer itemsCount,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal shipping,
        BigDecimal total
) {
}

