package com.rinas.revenue.domain;

/**
 * Authorization role within a single business.
 *
 * <p>OWNER manages the business and its users. ADMIN manages day-to-day data.
 * STAFF can record sales and read data but cannot manage users or destructive
 * settings. The role is always scoped to one business: a STAFF user of Business
 * A has no access to Business B at all, regardless of role.
 */
public enum Role {
    OWNER,
    ADMIN,
    STAFF
}
