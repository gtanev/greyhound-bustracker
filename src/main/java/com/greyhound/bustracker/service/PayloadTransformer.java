package com.greyhound.bustracker.service;

import com.greyhound.bustracker.domain.Carrier;
import com.greyhound.bustracker.domain.Driver;
import com.greyhound.bustracker.domain.Location;
import com.greyhound.bustracker.repository.CarrierRepository;
import com.greyhound.bustracker.repository.DriverRepository;
import com.greyhound.bustracker.repository.LocationRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PayloadTransformer {

  private final DriverRepository driverRepo;
  private final CarrierRepository carrierRepo;
  private final LocationRepository locationRepo;

  @Autowired
  public PayloadTransformer(DriverRepository driverRepo, CarrierRepository carrierRepo, LocationRepository locationRepo) {
    this.driverRepo = driverRepo;
    this.carrierRepo = carrierRepo;
    this.locationRepo = locationRepo;
  }

  @Transformer
  public Object transformPayload(Message<String> message) {
    final JSONObject jsonObject = new JSONObject(message.getPayload());

    Long driverId = jsonObject.getLong("oper_nbr");
    Long locationId = jsonObject.getLong("home_loc_6");
    String firstName = jsonObject.getString("first_name");
    String lastName = jsonObject.getString("last_name");
    String carrierName = jsonObject.getString("carrier_cd");
    String locationName = jsonObject.getString("home_loc_3");
    Object middleInit = jsonObject.get("middle_init");
    Object operClass = jsonObject.get("oper_class");

    final Driver driver = driverRepo.findByDriverId(driverId).orElse(new Driver(driverId));

    driver.setFirstName(formatString(firstName, false));
    driver.setLastName(formatString(lastName, false));
    driver.setMiddleInit(formatCharacter(middleInit));
    driver.setOperClass(formatCharacter(operClass));

    carrierName = formatString(carrierName, true);
    locationName = formatString(locationName, true);

    final Carrier carrier = carrierRepo.findByCarrierName(carrierName).orElse(new Carrier(carrierName));
    final Location location = locationRepo.findByLocationId(locationId).orElse(new Location(locationId));
    location.setLocationName(locationName);

    driver.setCarrier(carrier);
    driver.setLocation(location);

    return driver;
  }

  private String formatString(Object o, boolean allCaps) {
    if (o == JSONObject.NULL || o == null) return null;

    String trimmedString = String.valueOf(o).trim();

    if (trimmedString.isEmpty()) return null;

    return allCaps
        ? trimmedString.toUpperCase()
        : StringUtils.capitalize(trimmedString.toLowerCase());
  }

  private Character formatCharacter(Object o) {
    String formattedString = formatString(o, false);

    if (formattedString == null) return null;

    return formattedString.charAt(0);
  }
}
