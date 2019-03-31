package com.greyhound.bustracker.domain;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@NaturalIdCache
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
public class Carrier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NaturalId
  @NotNull
  private String carrierName;

  @Version
  @NotNull
  private Long version;

  public Carrier() {
  }

  public Carrier(@NotNull String carrierName) {
    this.carrierName = carrierName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCarrierName() {
    return carrierName;
  }

  public void setCarrierName(String carrierName) {
    this.carrierName = carrierName;
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
    if (!(o instanceof Carrier)) return false;
    Carrier carrier = (Carrier) o;
    return Objects.equals(carrierName, carrier.carrierName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(carrierName);
  }
}
