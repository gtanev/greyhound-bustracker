package com.greyhound.bustracker.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

  private Long id, locationId, version;
  private String name;

  @BeforeEach
  void init() {
    id = 1L;
    locationId = Long.MAX_VALUE;
    version = 0L;
    name = "LOC";
  }

  @Test
  void testNoArgConstructor() {
    final Location location = new Location();

    assertNotNull(location);
    assertNull(location.getId());
    assertNull(location.getLocationId());
    assertNull(location.getLocationName());
    assertNull(location.getVersion());
  }

  @Test
  void testParameterizedConstructor() {
    final Location location = new Location(locationId);

    assertNotNull(location);
    assertNull(location.getId());
    assertNotNull(location.getLocationId());
    assertEquals(locationId, location.getLocationId());
  }

  @Test
  void testSetters() {
    final Location location = new Location();
    location.setId(id);
    location.setLocationId(locationId);
    location.setLocationName(name);
    location.setVersion(version);

    assertNotNull(location);
    assertEquals(id, location.getId());
    assertEquals(locationId, location.getLocationId());
    assertEquals(name, location.getLocationName());
    assertEquals(version, location.getVersion());
  }

  @Test
  void testEqualsHashCode() {
    EqualsVerifier.forClass(Location.class)
        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
        .verify();
  }

}