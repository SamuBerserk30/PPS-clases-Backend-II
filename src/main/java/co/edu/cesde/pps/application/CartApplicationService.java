package co.edu.cesde.pps.application;

import co.edu.cesde.pps.dto.CartDTO;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.model.UserSession;
import co.edu.cesde.pps.service.CartService;
import co.edu.cesde.pps.service.UserSessionService;
import co.edu.cesde.pps.web.dto.request.AddCartItemRequest;
import co.edu.cesde.pps.web.dto.request.MergeGuestCartRequest;
import co.edu.cesde.pps.web.dto.request.UpdateCartItemQuantityRequest;
import co.edu.cesde.pps.web.dto.response.CartResponse;
import co.edu.cesde.pps.web.mapper.WebResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de carrito reutilizables para etapa12.
 */
@Service
@Transactional(readOnly = true)
public class CartApplicationService {

    private final CartService cartService;
    private final UserSessionService userSessionService;
    private final WebResponseMapper webResponseMapper;

    public CartApplicationService(CartService cartService,
                                  UserSessionService userSessionService,
                                  WebResponseMapper webResponseMapper) {
        this.cartService = cartService;
        this.userSessionService = userSessionService;
        this.webResponseMapper = webResponseMapper;
    }

    public CartResponse getCurrentCart(String sessionToken) {
        return webResponseMapper.toCartResponse(resolveCurrentCart(sessionToken));
    }

    @Transactional
    public CartResponse addItem(String sessionToken, AddCartItemRequest request) {
        CartDTO cart = resolveCurrentCart(sessionToken);
        return webResponseMapper.toCartResponse(
                cartService.addItem(cart.getCartId(), request.productId(), request.quantity())
        );
    }

    @Transactional
    public CartResponse updateItemQuantity(String sessionToken, Long productId,
                                           UpdateCartItemQuantityRequest request) {
        CartDTO cart = resolveCurrentCart(sessionToken);
        return webResponseMapper.toCartResponse(
                cartService.updateItemQuantity(cart.getCartId(), productId, request.quantity())
        );
    }

    @Transactional
    public CartResponse removeItem(String sessionToken, Long productId) {
        CartDTO cart = resolveCurrentCart(sessionToken);
        return webResponseMapper.toCartResponse(cartService.removeItem(cart.getCartId(), productId));
    }

    @Transactional
    public void clearCurrentCart(String sessionToken) {
        CartDTO cart = resolveCurrentCart(sessionToken);
        cartService.clearCart(cart.getCartId());
    }

    @Transactional
    public CartResponse mergeGuestCart(String sessionToken, MergeGuestCartRequest request) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toCartResponse(
                cartService.mergeGuestCartToUserCart(request.guestCartId(), user.getUserId())
        );
    }

    private CartDTO resolveCurrentCart(String sessionToken) {
        UserSession session = userSessionService.requireActiveSession(sessionToken);
        if (session.getUser() != null) {
            return cartService.findOrCreateOpenCartForUser(session.getUser().getUserId());
        }
        return cartService.findOrCreateOpenCartForGuestSession(session.getSessionId());
    }
}

