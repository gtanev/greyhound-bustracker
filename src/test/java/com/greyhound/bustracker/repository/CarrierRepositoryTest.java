package com.greyhound.bustracker.repository;

import com.greyhound.bustracker.domain.Carrier;
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
class CarrierRepositoryTest {

  @Autowired
  private CarrierRepository carrierRepository;

  private Carrier carrier;

  @BeforeEach
  void init() {
    carrier = new Carrier("CAR");
    carrierRepository.save(carrier);
  }

  @Test
  void testFindByCarrierId() {
    Optional<Carrier> persistedCarrier = carrierRepository.findByCarrierName(this.carrier.getCarrierName());

    assertTrue(persistedCarrier.isPresent());
    assertEquals(this.carrier, persistedCarrier.get());
  }

  @Test
  void testDeleteAndSave() {
    carrierRepository.deleteAll();

    Optional<Carrier> persistedCarrier = carrierRepository.findByCarrierName(this.carrier.getCarrierName());

    assertFalse(persistedCarrier.isPresent());

    carrierRepository.save(this.carrier);

    persistedCarrier = carrierRepository.findByCarrierName(this.carrier.getCarrierName());

    assertTrue(persistedCarrier.isPresent());
    assertEquals(this.carrier, persistedCarrier.get());
  }


  @Test
  void testQueryCache() {
    carrierRepository.findByCarrierName(this.carrier.getCarrierName());

    CacheManager cacheManager = CacheManager.ALL_CACHE_MANAGERS.get(0);

    assertTrue(Arrays.asList(cacheManager.getCacheNames()).contains("default-query-results-region"));
    assertTrue(cacheManager.getCache("default-query-results-region").getSize() > 0);
  }

}
