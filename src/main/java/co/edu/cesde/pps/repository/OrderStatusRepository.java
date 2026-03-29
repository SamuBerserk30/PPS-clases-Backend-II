package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
}
