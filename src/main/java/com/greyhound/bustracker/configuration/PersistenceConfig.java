package com.greyhound.bustracker.configuration;

import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jpa.dsl.Jpa;
import org.springframework.integration.jpa.dsl.JpaUpdatingOutboundEndpointSpec;
import org.springframework.integration.transaction.TransactionInterceptorBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.persistence.EntityManagerFactory;

@Configuration
public class PersistenceConfig {

  private final EntityManagerFactory entityManagerFactory;
  private final PlatformTransactionManager transactionManager;

  @Autowired
  public PersistenceConfig(EntityManagerFactory entityManagerFactory, PlatformTransactionManager transactionManager) {
    this.entityManagerFactory = entityManagerFactory;
    this.transactionManager = transactionManager;
  }

  @Bean
  public TransactionInterceptor transactionPropagator() {
    return new TransactionInterceptorBuilder(true)
        .transactionManager(transactionManager)
        .isolation(Isolation.READ_COMMITTED)
        .propagation(Propagation.REQUIRED)
        .build();
  }

  @Bean
  public JpaUpdatingOutboundEndpointSpec jpaOutboundAdapter() {
    return Jpa.outboundAdapter(entityManagerFactory);
  }

  @Bean
  CacheManager cacheManager() {
    return CacheManager.ALL_CACHE_MANAGERS.get(0);
  }

}
