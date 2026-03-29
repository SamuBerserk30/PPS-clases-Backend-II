package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
