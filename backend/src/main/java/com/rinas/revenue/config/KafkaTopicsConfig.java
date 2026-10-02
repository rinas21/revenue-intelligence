package com.rinas.revenue.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic provisioning. Declaring topics here has the same rationale as
 * Flyway owning the database schema: the set of topics is part of the system's
 * definition, is reviewed in the repository, and a fresh environment comes up
 * with the correct shape.
 *
 * <p>Naming convention: {@code revenue.<entity>.<event>}. Each event topic has a
 * matching {@code .DLT} topic where records that repeatedly fail processing are
 * parked, so a poison message cannot block a partition forever.
 */
@Configuration
class KafkaTopicsConfig {

    static final String ORDER_CREATED_TOPIC = "revenue.order-created";
    static final String ORDER_CANCELLED_TOPIC = "revenue.order-cancelled";
    static final String ORDER_CREATED_DLT = "revenue.order-created.DLT";
    static final String ORDER_CANCELLED_DLT = "revenue.order-cancelled.DLT";

    @Bean
    NewTopic orderCreatedTopic() {
        return topic(ORDER_CREATED_TOPIC);
    }

    @Bean
    NewTopic orderCancelledTopic() {
        return topic(ORDER_CANCELLED_TOPIC);
    }

    @Bean
    NewTopic orderCreatedDltTopic() {
        return topic(ORDER_CREATED_DLT);
    }

    @Bean
    NewTopic orderCancelledDltTopic() {
        return topic(ORDER_CANCELLED_DLT);
    }

    /**
     * Three partitions so a consumer group can scale, one replica because local
     * development runs a single broker. A real deployment raises replication to 3.
     */
    private static NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
