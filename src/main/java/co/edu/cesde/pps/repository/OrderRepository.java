package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNumberIgnoreCase(String orderNumber);

    Optional<Order> findByOrderNumberIgnoreCase(String orderNumber);

    List<Order> findByUser_UserId(Long userId);

    List<Order> findByOrderStatus_OrderStatusId(Long statusId);

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}
