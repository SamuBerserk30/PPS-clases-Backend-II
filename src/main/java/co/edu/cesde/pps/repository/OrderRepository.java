package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
