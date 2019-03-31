package com.greyhound.bustracker;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.greyhound.bustracker.configuration.IntegrationConfig;
import com.greyhound.bustracker.domain.Driver;
import com.greyhound.bustracker.repository.DriverRepository;
import com.greyhound.bustracker.service.PayloadTransformer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.http.dsl.HttpMessageHandlerSpec;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@PropertySource("classpath:application.properties")
class ApplicationTests {

  @Autowired
  private IntegrationConfig integrationConfig;
  @Autowired
  private DriverRepository driverRepository;
  @Autowired
  private PayloadTransformer payloadTransformer;

  private static final String API_HOST = "http://127.0.0.1";
  private static final int API_PORT = 1080;
  private static final int API_TIMEOUT = 5000;
  @Value("${api.retry-attempts:3}")
  private int RETRY_ATTEMPTS;
  @Value("${api.retry-delay-milliseconds:5000}")
  private int RETRY_DELAY;

  private static WireMockServer mockServer;
  private static String testData, testDataUpdated;
  private static StandardIntegrationFlow integrationFlow;

  private static final Object lock = new Object();
  private static RuntimeException runtimeException;
  private static boolean interrupted = false;

  @BeforeAll
  void init() throws IOException {
    mockServer = new WireMockServer(new WireMockConfiguration().port(API_PORT));
    mockServer.start();

    testData = new String(Files.readAllBytes(new ClassPathResource("test-data.json").getFile().toPath()));
    testDataUpdated = updateTestData(testData);

    integrationFlow = (StandardIntegrationFlow) integrationConfig.flow();
  }

  @Test
  @Order(0)
  void testApplicationContext() {
    Application.main(new String[]{});
  }

  @Test
  @Order(1)
  void testFlowStatus() {
    assertNotNull(integrationFlow);
    assertNotNull(integrationFlow.getIntegrationComponents());
    assertFalse(integrationFlow.getIntegrationComponents().isEmpty());
    assertFalse(integrationFlow.isRunning());
  }

  @Test
  @Order(2)
  void testRetries() {
    createStubForBadRequest();

    executeFlow(integrationFlow);

    mockServer.verify(RETRY_ATTEMPTS, getRequestedFor(urlEqualTo("/")));
    mockServer.resetRequests();

    assertFalse(integrationFlow.isRunning());

    Throwable exceptionRootCause = ExceptionUtils.getRootCause(runtimeException);
    assertTrue(exceptionRootCause instanceof HttpClientErrorException.BadRequest);
  }

  @Test
  @Order(3)
  void testRetriesWithTimeout() {
    createStubForTimedOutRequest();

    executeFlow(integrationFlow);

    mockServer.verify(RETRY_ATTEMPTS, getRequestedFor(urlEqualTo("/")));
    mockServer.resetRequests();

    assertFalse(integrationFlow.isRunning());

    Throwable exceptionRootCause = ExceptionUtils.getRootCause(runtimeException);
    assertTrue(exceptionRootCause instanceof SocketTimeoutException);
  }


  @Test
  @Order(4)
  @Transactional
  void testFlowExecutionWithInsert() {
    createStubForValidResponse1();

    executeFlow(integrationFlow);

    mockServer.verify(1, getRequestedFor(urlEqualTo("/")));
    mockServer.resetRequests();

    assertFalse(integrationFlow.isRunning());

    final List<Driver> drivers = driverRepository.findAll();

    assertNotNull(drivers);
    assertExpectedValues(drivers, testData);
  }

  @Test
  @Order(5)
  @Transactional
  void testFlowExecutionWithUpdate() {
    createStubForValidResponse2();

    executeFlow(integrationFlow);

    mockServer.verify(1, getRequestedFor(urlEqualTo("/")));
    mockServer.resetRequests();

    assertFalse(integrationFlow.isRunning());

    final List<Driver> drivers = driverRepository.findAll();

    assertNotNull(drivers);
    assertExpectedValues(drivers, testDataUpdated);

    assertEquals("Liberman", drivers.get(1).getLastName());
    assertEquals("CAR", drivers.get(1).getCarrier().getCarrierName());
    assertEquals('O', drivers.get(4).getOperClass());

    for (int i = 0; i < drivers.size(); i++) {
      if (i == 1 || i == 4)
        assertEquals(1, drivers.get(i).getVersion());
      else
        assertEquals(0, drivers.get(i).getVersion());
    }
  }

  private void executeFlow(StandardIntegrationFlow flow) {
    if (flow == null || flow.isRunning()) throw new IllegalArgumentException();

    long fallbackTimeout = ((API_TIMEOUT + RETRY_DELAY) * RETRY_ATTEMPTS) + 5000L;

    synchronized (lock) {
      flow.start();

      while (!interrupted) {
        try {
          lock.wait(fallbackTimeout);
          interrupted = true;
        } catch (InterruptedException ignored) {
        }
      }

      interrupted = false;
      flow.stop();
    }
  }

  private void assertExpectedValues(final List<Driver> drivers, String data) {
    final JSONArray records = new JSONObject(data).getJSONArray("results");

    assertEquals(records.length(), drivers.size());

    for (int i = 0; i < records.length(); i++) {
      int index = i;

      Driver persistedDriver = drivers.get(index);

      Driver expectedDriver = (Driver) payloadTransformer.transformPayload(new Message<String>() {
        @Override
        public String getPayload() {
          return String.valueOf(records.get(index));
        }

        @Override
        public MessageHeaders getHeaders() {
          return null;
        }
      });

      assertEquals(expectedDriver, persistedDriver);
      assertEquals(expectedDriver.getFirstName(), persistedDriver.getFirstName());
      assertEquals(expectedDriver.getLastName(), persistedDriver.getLastName());
      assertEquals(expectedDriver.getMiddleInit(), persistedDriver.getMiddleInit());
      assertEquals(expectedDriver.getOperClass(), persistedDriver.getOperClass());
      assertEquals(expectedDriver.getCarrier().getCarrierName(), persistedDriver.getCarrier().getCarrierName());
      assertEquals(expectedDriver.getLocation().getLocationId(), persistedDriver.getLocation().getLocationId());
      assertEquals(expectedDriver.getLocation().getLocationName(), persistedDriver.getLocation().getLocationName());
    }
  }

  private String updateTestData(final String testData) {
    final JSONArray records = new JSONObject(testData).getJSONArray("results");

    final JSONObject driver1 = records.getJSONObject(1);
    final JSONObject driver4 = records.getJSONObject(4);

    driver1.put("last_name", "LIBERMAN");
    driver1.put("carrier_cd", "CAR");
    driver4.put("oper_class", "O");

    records.put(1, driver1);
    records.put(4, driver4);

    return new JSONObject().put("results", records).toString();
  }

  private StubMapping createStubForBadRequest() {
    return mockServer.stubFor(
        WireMock.get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody("bad request"))
    );
  }

  private StubMapping createStubForTimedOutRequest() {
    return mockServer.stubFor(
        WireMock.get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody("{}")
                .withFixedDelay(API_TIMEOUT * 2))
    );
  }

  private StubMapping createStubForValidResponse1() {
    return mockServer.stubFor(
        WireMock.get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody(testData))
    );
  }

  private StubMapping createStubForValidResponse2() {
    return mockServer.stubFor(
        WireMock.get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody(testDataUpdated))
    );
  }

  @AfterAll
  void cleanup() {
    mockServer.stop();
    mockServer.shutdown();
  }

  @TestConfiguration
  static class ContextConfiguration {

    @Bean
    @Primary
    public HttpMessageHandlerSpec httpRequestHandler() {
      return Http.outboundGateway(API_HOST + ":" + API_PORT)
          .httpMethod(HttpMethod.GET)
          .expectedResponseType(String.class)
          .requestFactory(httpRequestFactory());
    }

    @Bean
    @Primary
    public SimpleClientHttpRequestFactory httpRequestFactory() {
      SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
      httpRequestFactory.setReadTimeout(API_TIMEOUT);
      return httpRequestFactory;
    }

    @ServiceActivator(inputChannel = "errorChannel")
    public void processErrors(Exception message) {
      synchronized (lock) {
        interrupted = true;
        runtimeException = new RuntimeException(message);
        lock.notifyAll();
      }
    }
  }

}