package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNameIgnoreCase(String name);
}
