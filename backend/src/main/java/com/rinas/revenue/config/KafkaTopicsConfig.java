package com.rinas.revenue.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic provisioning.
 *
 * <p>Declaring topics here rather than creating them by hand has the same
 * rationale as Flyway owning the database schema: the set of topics is part of
 * the system's definition, it is reviewed in the repository, and a fresh
 * environment comes up with the correct shape without manual steps.
 *
 * <p>Naming convention: {@code revenue.<entity>.<event>}, all lowercase, dots as
 * separators — {@code revenue.order-created}, {@code revenue.order-cancelled},
 * {@code revenue.refund-created}. The prefix keeps platform topics in one
 * namespace and leaves room for a different prefix later if another producer
 * ever needs to share the cluster.
 *
 * <p>What is deliberately absent: no {@code KafkaTemplate} send, no
 * {@code @KafkaListener}, no serializer configuration. Those belong to Phase 3
 * and Phase 4, and configuring a {@code JsonSerializer} against event types
 * that do not exist yet would be guessing rather than designing. The broker
 * connection, the health indicator and the topic itself are the whole of the
 * Phase 1 Kafka surface.
 */
@Configuration
class KafkaTopicsConfig {

    static final String ORDER_CREATED_TOPIC = "revenue.order-created";

    /**
     * Partitions are 3 so that a future consumer group can scale horizontally;
     * replication is 1 because local development runs a single broker, and a
     * replication factor above the broker count would leave the topic
     * permanently under-replicated. A real deployment raises this to 3.
     */
    @Bean
    NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
