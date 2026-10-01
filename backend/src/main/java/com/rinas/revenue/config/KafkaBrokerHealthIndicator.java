package com.rinas.revenue.config;

import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

/**
 * Reports broker reachability on {@code /actuator/health}.
 *
 * <p>Spring Boot 4 ships no Kafka health indicator, so without this the health
 * endpoint would report PostgreSQL and Redis and silently say nothing about the
 * event backbone — which reads as "the broker is fine" when it may not be.
 *
 * <p>The check asks the broker to describe its cluster and waits a bounded three
 * seconds. A health endpoint that blocks indefinitely is worse than one that
 * reports a timeout, because everything monitoring it stalls behind it.
 */
@Component("kafka")
class KafkaBrokerHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(KafkaBrokerHealthIndicator.class);

    private static final long CLUSTER_DESCRIPTION_TIMEOUT_SECONDS = 3;

    private final KafkaAdmin kafkaAdmin;

    KafkaBrokerHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public Health health() {
        DescribeClusterResult cluster;
        try (Admin admin = Admin.create(kafkaAdmin.getConfigurationProperties())) {
            cluster = admin.describeCluster();
            var nodes = cluster.nodes().get(CLUSTER_DESCRIPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return Health.up()
                .withDetail("nodes", nodes.stream().map(Node::id).toList())
                .withDetail("nodeCount", nodes.size())
                .build();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return down("Interrupted while contacting the broker", ex);
        } catch (Exception ex) {
            return down("Broker is not reachable", ex);
        }
    }

    private static Health down(String reason, Exception ex) {
        log.debug("Kafka broker health check failed: {}", reason, ex);
        return Health.down(ex).withDetail("reason", reason).build();
    }
}
