package com.greyhound.bustracker.configuration;

import com.greyhound.bustracker.service.PayloadSplitter;
import com.greyhound.bustracker.service.PayloadTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.retry.annotation.EnableRetry;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableIntegration
@EnableRetry
public class IntegrationConfig {

  @Value("${api.polling-interval-minutes}")
  private int POLL_INTERVAL;

  private final ApiConfig apiConfig;
  private final PersistenceConfig persistenceConfig;
  private final PayloadSplitter payloadSplitter;
  private final PayloadTransformer payloadTransformer;

  @Autowired
  public IntegrationConfig(ApiConfig apiConfig, PersistenceConfig persistenceConfig,
                           PayloadSplitter payloadSplitter, PayloadTransformer payloadTransformer) {
    this.apiConfig = apiConfig;
    this.persistenceConfig = persistenceConfig;
    this.payloadSplitter = payloadSplitter;
    this.payloadTransformer = payloadTransformer;
  }

  @Bean
  public IntegrationFlow flow() {
    return IntegrationFlows
        .from(() -> new GenericMessage<>(""),
            e -> e.poller(poller()).autoStartup(true))
        .wireTap(f -> f.handle(e -> persistenceConfig.cacheManager().clearAll()))
        .handle(apiConfig.httpRequestHandler(),
            e -> e.advice(apiConfig.retryAdvice()))
        .split(payloadSplitter)
        .transform(payloadTransformer, "transformPayload",
            e -> e.transactional(persistenceConfig.transactionPropagator()))
        .handle(persistenceConfig.jpaOutboundAdapter())
        .get();
  }

  @Bean(name = PollerMetadata.DEFAULT_POLLER)
  public PollerMetadata poller() {
    return Pollers.fixedRate(POLL_INTERVAL, TimeUnit.MINUTES).get();
  }

}
