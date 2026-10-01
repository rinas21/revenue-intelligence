package com.rinas.revenue.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    static final String ORDER_CREATED_TOPIC = "revenue.order-created";

    @Bean
    NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }
}