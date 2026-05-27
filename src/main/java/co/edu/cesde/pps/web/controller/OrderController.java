package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.OrderApplicationService;
import co.edu.cesde.pps.web.dto.request.CheckoutRequest;
import co.edu.cesde.pps.web.dto.response.OrderResponse;
import co.edu.cesde.pps.web.security.CurrentSessionResolver;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.ORDERS)
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public OrderController(OrderApplicationService orderApplicationService,
                           CurrentSessionResolver currentSessionResolver) {
        this.orderApplicationService = orderApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                                  String authorizationHeader,
                                                  @Valid @RequestBody CheckoutRequest request) {
        OrderResponse response = orderApplicationService.checkout(
                currentSessionResolver.resolveCurrentToken(authorizationHeader),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public List<OrderResponse> listMyOrders(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                            String authorizationHeader) {
        return orderApplicationService.listMyOrders(currentSessionResolver.resolveCurrentToken(authorizationHeader));
    }

    @GetMapping("/{id}")
    public OrderResponse getMyOrder(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                    String authorizationHeader,
                                    @PathVariable Long id) {
        return orderApplicationService.getMyOrder(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
    }
}

