package com.banking.backend.infrastructure.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class KafkaConsumerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfiguration.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            // After bounded retries, leave the failed record visible in logs for operator review.
            log.error("Kafka record failed after retries: topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset(), exception);
        }, new FixedBackOff(1_000L, 3L));
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, IllegalStateException.class);
        return errorHandler;
    }
}
