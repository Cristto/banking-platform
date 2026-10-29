package com.banking.backend.infrastructure.kafka;

import com.banking.backend.infrastructure.kafka.purchase.RefundEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
public class RefundKafkaConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundEvent>
            refundKafkaListenerContainerFactory(KafkaProperties properties) {

        var consumerProperties = properties.buildConsumerProperties();

        consumerProperties.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                RefundEvent.class.getName()
        );

        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(RefundEvent.class, false)
        );

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, RefundEvent>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}