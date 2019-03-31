package com.greyhound.bustracker.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarrierTest {

  private Long id, version;
  private String name;

  @BeforeEach
  void init() {
    id = Long.MAX_VALUE;
    version = 0L;
    name = "CAR";
  }

  @Test
  void testNoArgConstructor() {
    final Carrier carrier = new Carrier();

    assertNotNull(carrier);
    assertNull(carrier.getId());
    assertNull(carrier.getCarrierName());
    assertNull(carrier.getVersion());
  }

  @Test
  void testParameterizedConstructor() {
    final Carrier carrier = new Carrier(name);

    assertNotNull(carrier);
    assertNull(carrier.getId());
    assertNotNull(carrier.getCarrierName());
    assertEquals(name, carrier.getCarrierName());
  }

  @Test
  void testSetters() {
    final Carrier carrier = new Carrier();
    carrier.setId(id);
    carrier.setCarrierName(name);
    carrier.setVersion(version);

    assertNotNull(carrier);
    assertEquals(id, carrier.getId());
    assertEquals(name, carrier.getCarrierName());
    assertEquals(version, carrier.getVersion());
  }

  @Test
  void testEqualsHashCode() {
    EqualsVerifier.forClass(Carrier.class)
        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
        .verify();
  }

}