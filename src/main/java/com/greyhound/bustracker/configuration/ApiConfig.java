package com.greyhound.bustracker.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.http.dsl.HttpMessageHandlerSpec;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class ApiConfig {

  @Value("${api.url}")
  private String API_URL;

  @Value("${api.response-timeout-milliseconds}")
  private int API_TIMEOUT;

  @Value("${api.retry-attempts}")
  private int RETRY_ATTEMPTS;

  @Value("${api.retry-delay-milliseconds}")
  private int RETRY_DELAY;

  @Bean
  public HttpMessageHandlerSpec httpRequestHandler() {
    return Http.outboundGateway(API_URL)
        .httpMethod(HttpMethod.GET)
        .expectedResponseType(String.class)
        .requestFactory(httpRequestFactory());
  }

  @Bean
  public SimpleClientHttpRequestFactory httpRequestFactory() {
    SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
    httpRequestFactory.setReadTimeout(API_TIMEOUT);
    return httpRequestFactory;
  }

  @Bean
  public SimpleRetryPolicy retryPolicy() {
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(RETRY_ATTEMPTS);
    return retryPolicy;
  }

  @Bean
  public FixedBackOffPolicy backOffPolicy() {
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(RETRY_DELAY);
    return backOffPolicy;
  }

  @Bean
  public RequestHandlerRetryAdvice retryAdvice() {
    RequestHandlerRetryAdvice retryAdvice = new RequestHandlerRetryAdvice();
    RetryTemplate retryTemplate = new RetryTemplate();
    retryTemplate.setBackOffPolicy(backOffPolicy());
    retryTemplate.setRetryPolicy(retryPolicy());
    retryAdvice.setRetryTemplate(retryTemplate);
    return retryAdvice;
  }

}
