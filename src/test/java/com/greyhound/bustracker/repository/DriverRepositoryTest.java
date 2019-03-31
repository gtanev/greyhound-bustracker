package com.greyhound.bustracker.repository;

import com.greyhound.bustracker.domain.Carrier;
import com.greyhound.bustracker.domain.Driver;
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
class DriverRepositoryTest {

  @Autowired
  private DriverRepository driverRepository;

  private Driver driver;

  @BeforeEach
  void init() {
    driver = new Driver(1L);
    driver.setFirstName("John");
    driver.setLastName("Doe");
    driver.setMiddleInit('M');
    driver.setOperClass('O');
    driver.setLocation(new Location());
    driver.setCarrier(new Carrier());
    driver.getCarrier().setCarrierName("XYZ");
    driver.getLocation().setLocationId(2L);
    driverRepository.save(driver);
  }

  @Test
  void testFindByDriverId() {
    Optional<Driver> persistedDriver = driverRepository.findByDriverId(this.driver.getDriverId());

    assertTrue(persistedDriver.isPresent());
    assertEquals(this.driver, persistedDriver.get());
  }

  @Test
  void testDeleteAndSave() {
    driverRepository.deleteAll();

    Optional<Driver> persistedDriver = driverRepository.findByDriverId(this.driver.getDriverId());

    assertFalse(persistedDriver.isPresent());

    driverRepository.save(this.driver);

    persistedDriver = driverRepository.findByDriverId(this.driver.getDriverId());

    assertTrue(persistedDriver.isPresent());
    assertEquals(this.driver, persistedDriver.get());
  }

  @Test
  void testQueryCache() {
    driverRepository.findByDriverId(this.driver.getDriverId());

    CacheManager cacheManager = CacheManager.ALL_CACHE_MANAGERS.get(0);

    assertTrue(Arrays.asList(cacheManager.getCacheNames()).contains("default-query-results-region"));
    assertTrue(cacheManager.getCache("default-query-results-region").getSize() > 0);
  }

}
