package com.greyhound.bustracker.repository;

import com.greyhound.bustracker.domain.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
  @QueryHints(value = @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true"))
  Optional<Driver> findByDriverId(Long driverId);
}
