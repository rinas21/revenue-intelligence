package com.rinas.revenue.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Fixed-window rate limiting, enforced in Redis so it is shared across instances. */
@Component
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private int capacity = 120;
    private Duration window = Duration.ofMinutes(1);

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Duration getWindow() {
        return window;
    }

    public void setWindow(Duration window) {
        this.window = window;
    }
}
