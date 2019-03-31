package com.greyhound.bustracker.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@NaturalIdCache
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
public class Driver {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NaturalId
  @NotNull
  private Long driverId;

  @NotNull
  private String firstName;

  @NotNull
  private String lastName;

  private Character middleInit;

  private Character operClass;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "carrier_id")
  private Carrier carrier;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

  @Version
  @NotNull
  private Long version;

  public Driver() {
  }

  public Driver(@NotNull Long driverId) {
    this.driverId = driverId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getDriverId() {
    return driverId;
  }

  public void setDriverId(Long driverId) {
    this.driverId = driverId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Character getMiddleInit() {
    return middleInit;
  }

  public void setMiddleInit(Character middleInit) {
    this.middleInit = middleInit;
  }

  public Character getOperClass() {
    return operClass;
  }

  public void setOperClass(Character operClass) {
    this.operClass = operClass;
  }

  public Carrier getCarrier() {
    return carrier;
  }

  public void setCarrier(Carrier carrier) {
    this.carrier = carrier;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Driver)) return false;
    Driver driver = (Driver) o;
    return Objects.equals(driverId, driver.driverId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(driverId);
  }
}
