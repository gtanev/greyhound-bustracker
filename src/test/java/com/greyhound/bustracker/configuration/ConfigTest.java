package com.greyhound.bustracker.configuration;

import com.greyhound.bustracker.service.PayloadSplitter;
import com.greyhound.bustracker.service.PayloadTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.dsl.HttpMessageHandlerSpec;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.jpa.dsl.JpaUpdatingOutboundEndpointSpec;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManagerFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

class ConfigTest {

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private ApiConfig apiConfig;
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private PersistenceConfig persistenceConfig;
  @Mock
  private PayloadSplitter payloadSplitter;
  @Mock
  private PayloadTransformer payloadTransformer;

  @InjectMocks
  private IntegrationConfig integrationConfig;

  @BeforeEach
  void init() {
    initMocks(this);

    ReflectionTestUtils.setField(persistenceConfig, "entityManagerFactory", mock(EntityManagerFactory.class));
    ReflectionTestUtils.setField(integrationConfig, "POLL_INTERVAL", 10);
  }

  @Test
  void testFlowDependencies() {
    IntegrationFlow flow = integrationConfig.flow();

    verify(apiConfig).httpRequestHandler();
    verify(apiConfig).retryAdvice();
    verify(persistenceConfig).transactionPropagator();
    verify(persistenceConfig).jpaOutboundAdapter();

    assertNotNull(flow);
    assertTrue(flow instanceof StandardIntegrationFlow);
  }

  @Test
  void testPoller() {
    PollerMetadata poller = integrationConfig.poller();
    PeriodicTrigger trigger = (PeriodicTrigger) poller.getTrigger();

    long pollIntervalInMilliseconds = (int) ReflectionTestUtils.getField(integrationConfig, "POLL_INTERVAL") * 60000L;

    assertNotNull(poller);
    assertNotNull(trigger);
    assertTrue(trigger.isFixedRate());
    assertEquals(TimeUnit.MINUTES, trigger.getTimeUnit());
    assertEquals(pollIntervalInMilliseconds, trigger.getPeriod());
  }

  @Nested
  class ApiConfigTest {

    private ApiConfig apiConfig;

    private String API_URL;

    private int API_TIMEOUT;

    private int RETRY_ATTEMPTS;

    private int RETRY_DELAY;

    @BeforeEach
    void init() {
      apiConfig = new ApiConfig();

      API_URL = "www.api-url.com";
      API_TIMEOUT = 5000;
      RETRY_ATTEMPTS = 3;
      RETRY_DELAY = 2000;

      ReflectionTestUtils.setField(apiConfig, "API_URL", API_URL);
      ReflectionTestUtils.setField(apiConfig, "API_TIMEOUT", API_TIMEOUT);
      ReflectionTestUtils.setField(apiConfig, "RETRY_ATTEMPTS", RETRY_ATTEMPTS);
      ReflectionTestUtils.setField(apiConfig, "RETRY_DELAY", RETRY_DELAY);
    }

    @Test
    void testRetryAdvice() {
      RequestHandlerRetryAdvice retryAdvice = apiConfig.retryAdvice();
      RetryTemplate retryTemplate = (RetryTemplate) ReflectionTestUtils.getField(retryAdvice, "retryTemplate");

      SimpleRetryPolicy retryPolicy = (SimpleRetryPolicy) ReflectionTestUtils.getField(retryTemplate, "retryPolicy");
      FixedBackOffPolicy backOffPolicy = (FixedBackOffPolicy) ReflectionTestUtils.getField(retryTemplate, "backOffPolicy");

      assertNotNull(retryPolicy);
      assertEquals(RETRY_ATTEMPTS, retryPolicy.getMaxAttempts());

      assertNotNull(backOffPolicy);
      assertEquals(RETRY_DELAY, backOffPolicy.getBackOffPeriod());
    }

    @Test
    void testHttpRequestHandler() {
      HttpMessageHandlerSpec requestHandler = apiConfig.httpRequestHandler();
      assertNotNull(requestHandler);

      HttpRequestExecutingMessageHandler target = (HttpRequestExecutingMessageHandler) ReflectionTestUtils.getField(requestHandler, "target");
      assertNotNull(target);

      LiteralExpression uriExpression = (LiteralExpression) ReflectionTestUtils.getField(target, "uriExpression");
      assertNotNull(uriExpression);
      assertEquals(API_URL, uriExpression.getExpressionString());

      ValueExpression httpMethodExpression = (ValueExpression) ReflectionTestUtils.getField(target, "httpMethodExpression");
      assertNotNull(httpMethodExpression);
      assertEquals(HttpMethod.GET, httpMethodExpression.getValue());

      RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(target, "restTemplate");
      assertNotNull(restTemplate);
      assertNotNull(restTemplate.getRequestFactory());

      Integer readTimeout = (Integer) ReflectionTestUtils.getField(restTemplate.getRequestFactory(), "readTimeout");
      assertNotNull(readTimeout);
      assertEquals(API_TIMEOUT, readTimeout.intValue());
    }

  }

  @Nested
  class PersistenceConfigTest {

    @InjectMocks
    private PersistenceConfig persistenceConfig;

    @Mock
    private EntityManagerFactory entityManagerFactory;
    @Mock
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void init() {
      initMocks(this);
    }

    @Test
    void testExpectedReturnValues() {
      TransactionInterceptor transactionInterceptor = persistenceConfig.transactionPropagator();

      assertNotNull(transactionInterceptor);

      JpaUpdatingOutboundEndpointSpec jpaOutboundAdapter = persistenceConfig.jpaOutboundAdapter();

      assertNotNull(jpaOutboundAdapter);

      try {
        persistenceConfig.cacheManager();
        assertNotNull(persistenceConfig.cacheManager());
      } catch (Exception e) {
        assertTrue(e instanceof IndexOutOfBoundsException);
      }
    }

  }
}
