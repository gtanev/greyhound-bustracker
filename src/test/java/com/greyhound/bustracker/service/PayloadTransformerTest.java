package com.greyhound.bustracker.service;

import com.greyhound.bustracker.domain.Driver;
import com.greyhound.bustracker.repository.CarrierRepository;
import com.greyhound.bustracker.repository.DriverRepository;
import com.greyhound.bustracker.repository.LocationRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

class PayloadTransformerTest {

  private PayloadTransformer payloadTransformer;
  private Message<String> message;

  @Mock
  private DriverRepository driverRepo;
  @Mock
  private CarrierRepository carrierRepo;
  @Mock
  private LocationRepository locationRepo;

  @BeforeEach
  void init() throws IOException {
    initMocks(this);

    Resource resource = new ClassPathResource("test-data.json");
    String jsonPayload = new String(Files.readAllBytes(resource.getFile().toPath()));
    Object singleObject = new JSONObject(jsonPayload).getJSONArray("results").get(0);

    message = new Message<String>() {
      @Override
      public String getPayload() {
        return String.valueOf(singleObject);
      }

      @Override
      public MessageHeaders getHeaders() {
        return null;
      }
    };

    payloadTransformer = new PayloadTransformer(driverRepo, carrierRepo, locationRepo);
  }

  @Test
  void testTransformer() {
    Object driver = payloadTransformer.transformPayload(message);

    verify(driverRepo).findByDriverId(any(Long.class));
    verify(locationRepo).findByLocationId(any(Long.class));
    verify(carrierRepo).findByCarrierName(any(String.class));

    assertTrue(driver instanceof Driver);
    assertEquals(387743L, ((Driver) driver).getDriverId().longValue());
    assertEquals("Terry", ((Driver) driver).getFirstName());
    assertEquals("V", ((Driver) driver).getMiddleInit().toString());
    assertEquals("Jenkins", ((Driver) driver).getLastName());
    assertEquals("R", ((Driver) driver).getOperClass().toString());
    assertNotNull(((Driver) driver).getCarrier());
    assertNotNull(((Driver) driver).getLocation());
  }

  @Test
  void testFormatString() {
    try {
      Method formatStringMethod = PayloadTransformer.class.getDeclaredMethod("formatString", Object.class, boolean.class);
      formatStringMethod.setAccessible(true);

      assertAll("(1) Capitalized, (2) ALL_CAPS, (3)(4)(5) NULL",
          () -> {
            String testString = "STRING ";
            Object formattedString = formatStringMethod.invoke(payloadTransformer, testString, false);
            assertEquals("String", formattedString);
          },
          () -> {
            String testString = " String";
            Object formattedString = formatStringMethod.invoke(payloadTransformer, testString, true);
            assertEquals("STRING", formattedString);
          },
          () -> {
            String testString = " ";
            Object formattedString = formatStringMethod.invoke(payloadTransformer, testString, false);
            assertNull(formattedString);
          },
          () -> {
            String testString = "";
            Object formattedString = formatStringMethod.invoke(payloadTransformer, testString, false);
            assertNull(formattedString);
          },
          () -> {
            Object formattedString = formatStringMethod.invoke(payloadTransformer, null, false);
            assertNull(formattedString);
          }
      );
    } catch (NoSuchMethodException e) {
      fail(e);
    }
  }

  @Test
  void testFormatCharacter() {
    try {
      Method formatCharacterMethod = PayloadTransformer.class.getDeclaredMethod("formatCharacter", Object.class);
      formatCharacterMethod.setAccessible(true);

      assertAll("(1)(2) Capitalized, (3)(4) NULL",
          () -> {
            String testString = " string";
            Object formattedCharacter = formatCharacterMethod.invoke(payloadTransformer, testString);
            assertEquals('S', formattedCharacter);
          },
          () -> {
            String testString = "S ";
            Object formattedCharacter = formatCharacterMethod.invoke(payloadTransformer, testString);
            assertEquals('S', formattedCharacter);
          },
          () -> {
            String testString = " ";
            Object formattedCharacter = formatCharacterMethod.invoke(payloadTransformer, testString);
            assertNull(formattedCharacter);
          },
          () -> {
            String testString = "";
            Object formattedCharacter = formatCharacterMethod.invoke(payloadTransformer, testString);
            assertNull(formattedCharacter);
          }
      );
    } catch (NoSuchMethodException e) {
      fail(e);
    }
  }

}
