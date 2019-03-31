package com.greyhound.bustracker.repository;

import com.greyhound.bustracker.domain.Location;
import net.sf.ehcache.CacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class LocationRepositoryTest {

  @Autowired
  private LocationRepository locationRepository;

  private Location location;

  @BeforeEach
  void init() {
    location = new Location(Long.MAX_VALUE);
    location.setLocationName("LOC");
    locationRepository.save(location);
  }

  @Test
  void testFindByLocationId() {
    Optional<Location> persistedLocation = locationRepository.findByLocationId(this.location.getLocationId());

    assertTrue(persistedLocation.isPresent());
    assertEquals(this.location, persistedLocation.get());
  }

  @Test
  void testDeleteAndSave() {
    locationRepository.deleteAll();

    Optional<Location> persistedLocation = locationRepository.findByLocationId(this.location.getLocationId());

    assertFalse(persistedLocation.isPresent());

    locationRepository.save(this.location);

    persistedLocation = locationRepository.findByLocationId(this.location.getLocationId());

    assertTrue(persistedLocation.isPresent());
    assertEquals(this.location, persistedLocation.get());
  }


  @Test
  void testQueryCache() {
    locationRepository.findByLocationId(this.location.getLocationId());

    CacheManager cacheManager = CacheManager.ALL_CACHE_MANAGERS.get(0);

    assertTrue(Arrays.asList(cacheManager.getCacheNames()).contains("default-query-results-region"));
    assertTrue(cacheManager.getCache("default-query-results-region").getSize() > 0);
  }

}
