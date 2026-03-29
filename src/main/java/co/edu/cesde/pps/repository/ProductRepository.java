package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
