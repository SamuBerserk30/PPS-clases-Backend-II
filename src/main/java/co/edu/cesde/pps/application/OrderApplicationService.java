package co.edu.cesde.pps.application;

import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.service.OrderService;
import co.edu.cesde.pps.service.UserSessionService;
import co.edu.cesde.pps.web.dto.request.CheckoutRequest;
import co.edu.cesde.pps.web.dto.response.OrderResponse;
import co.edu.cesde.pps.web.mapper.WebResponseMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de órdenes listos para ser invocados por controllers futuros.
 */
@Service
@Transactional(readOnly = true)
public class OrderApplicationService {

    private final OrderService orderService;
    private final UserSessionService userSessionService;
    private final WebResponseMapper webResponseMapper;

    public OrderApplicationService(OrderService orderService,
                                   UserSessionService userSessionService,
                                   WebResponseMapper webResponseMapper) {
        this.orderService = orderService;
        this.userSessionService = userSessionService;
        this.webResponseMapper = webResponseMapper;
    }

    @Transactional
    public OrderResponse checkout(String sessionToken, CheckoutRequest request) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toOrderResponse(orderService.checkout(
                user.getUserId(),
                request.cartId(),
                request.shippingAddressId(),
                request.billingAddressId()
        ));
    }

    public List<OrderResponse> listMyOrders(String sessionToken) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toOrderResponseList(orderService.findOrdersByUser(user.getUserId()));
    }

    public OrderResponse getMyOrder(String sessionToken, Long orderId) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toOrderResponse(orderService.findByIdForUser(user.getUserId(), orderId));
    }
}

