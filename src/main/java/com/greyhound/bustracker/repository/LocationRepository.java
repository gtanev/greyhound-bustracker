package com.greyhound.bustracker.repository;

import com.greyhound.bustracker.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
  @QueryHints(value = @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true"))
  Optional<Location> findByLocationId(Long locationId);
}
