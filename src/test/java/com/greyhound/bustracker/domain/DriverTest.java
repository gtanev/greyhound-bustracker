package com.greyhound.bustracker.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DriverTest {

  private Long id, driverId, version;
  private String firstName, lastName;
  private Character middleInit, operClass;
  private Carrier carrier;
  private Location location;

  @BeforeEach
  void init() {
    id = Long.MIN_VALUE;
    driverId = Long.MAX_VALUE;
    version = 0L;
    firstName = "John";
    lastName = "Doe";
    middleInit = 'M';
    operClass = 'O';
    carrier = new Carrier();
    location = new Location();
    carrier.setCarrierName("XYZ");
    location.setLocationId(2L);
  }

  @Test
  void testNoArgConstructor() {
    final Driver driver = new Driver();

    assertNotNull(driver);
    assertNull(driver.getId());
    assertNull(driver.getDriverId());
    assertNull(driver.getFirstName());
    assertNull(driver.getMiddleInit());
    assertNull(driver.getLastName());
    assertNull(driver.getOperClass());
    assertNull(driver.getVersion());
    assertNull(driver.getLocation());
    assertNull(driver.getCarrier());
  }

  @Test
  void testParameterizedConstructor() {
    final Driver driver = new Driver(driverId);

    assertNotNull(driver);
    assertNull(driver.getId());
    assertNotNull(driver.getDriverId());
    assertEquals(driverId, driver.getDriverId());
  }

  @Test
  void testSetters() {
    final Driver driver = new Driver();
    driver.setId(id);
    driver.setDriverId(driverId);
    driver.setFirstName(firstName);
    driver.setMiddleInit(middleInit);
    driver.setLastName(lastName);
    driver.setOperClass(operClass);
    driver.setVersion(version);
    driver.setLocation(location);
    driver.setCarrier(carrier);

    assertNotNull(driver);
    assertEquals(id, driver.getId());
    assertEquals(driverId, driver.getDriverId());
    assertEquals(firstName, driver.getFirstName());
    assertEquals(middleInit, driver.getMiddleInit());
    assertEquals(lastName, driver.getLastName());
    assertEquals(operClass, driver.getOperClass());
    assertEquals(version, driver.getVersion());
    assertEquals(location, driver.getLocation());
    assertEquals(carrier, driver.getCarrier());
  }

  @Test
  void testEqualsHashCode() {
    EqualsVerifier.forClass(Driver.class)
        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
        .verify();
  }

}