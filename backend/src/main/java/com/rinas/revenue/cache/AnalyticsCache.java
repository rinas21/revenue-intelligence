package com.rinas.revenue.cache;

import java.util.Optional;
import java.util.UUID;

/**
 * Cache of expensive analytics responses.
 *
 * <p>Every logical key is namespaced by business id, so a cache entry produced
 * for one business can never be returned for another. Invalidation is also
 * always business-scoped. This is a deliberate design choice: cross-tenant
 * leakage through a shared cache key is one of the most common multi-tenant
 * bugs, and the key builder makes it structurally impossible here.
 */
public interface AnalyticsCache {

    Optional<String> get(UUID businessId, String logicalKey);

    void put(UUID businessId, String logicalKey, String json, long ttlSeconds);

    void invalidateBusiness(UUID businessId);
}
