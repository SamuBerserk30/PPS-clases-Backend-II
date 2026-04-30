package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    long countByUser_UserId(Long userId);

    List<Address> findByUser_UserId(Long userId);
}
