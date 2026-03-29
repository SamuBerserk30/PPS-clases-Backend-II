package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
}
