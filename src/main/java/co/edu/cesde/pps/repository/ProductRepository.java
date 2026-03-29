package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<Product> findBySkuIgnoreCase(String sku);

    List<Product> findByIsActiveTrue();

    List<Product> findByCategory_CategoryId(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);

}
