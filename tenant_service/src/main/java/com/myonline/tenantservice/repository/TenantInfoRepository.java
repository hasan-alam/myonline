package com.myonline.tenantservice.repository;

import com.myonline.tenantservice.entity.TenantInfo;
import com.myonline.tenantservice.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link TenantInfo} entity.
 * Provides CRUD operations and domain prefix lookups for active tenant accounts.
 */
@Repository
public interface TenantInfoRepository extends JpaRepository<TenantInfo, Long> {

    /**
     * Checks whether a domain prefix is already in use by an active tenant account.
     *
     * @param domainPrefix the domain prefix to check
     * @return true if a tenant with this domain prefix exists
     */
    boolean existsByDomainPrefix(String domainPrefix);

    /**
     * Finds a tenant account by its domain prefix.
     *
     * @param domainPrefix the domain prefix to search for
     * @return an Optional containing the tenant, or empty if not found
     */
    Optional<TenantInfo> findByDomainPrefix(String domainPrefix);

    /**
     * Returns all tenants filtered by their status.
     *
     * @param status the tenant status (A=Active, I=Inactive)
     * @return list of tenants with the given status
     */
    List<TenantInfo> findByStatus(TenantStatus status);

    /**
     * Checks whether a domain prefix is in use by an active tenant account
     * (used for domain availability check alongside registration requests).
     *
     * @param domainPrefix the domain prefix to check
     * @param status       the status to filter by (e.g., A=Active)
     * @return true if a tenant with this domain prefix and status exists
     */
    boolean existsByDomainPrefixAndStatus(String domainPrefix, TenantStatus status);
}
