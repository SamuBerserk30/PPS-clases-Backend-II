package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
