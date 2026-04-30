package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.enums.CartStatus;
import co.edu.cesde.pps.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_UserIdAndStatus(Long userId, CartStatus status);
}
