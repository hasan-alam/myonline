package com.myonline.tenantservice.repository;

import com.myonline.tenantservice.entity.TenantRegistrationRequest;
import com.myonline.tenantservice.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link TenantRegistrationRequest} entity.
 * Provides CRUD and custom filtered search operations.
 */
@Repository
public interface TenantRegistrationRepository extends JpaRepository<TenantRegistrationRequest, Long> {

    /**
     * Checks whether a domain prefix has already been used in a registration request.
     *
     * @param domainPrefix the domain prefix to check
     * @return true if a registration request with this domain prefix already exists
     */
    boolean existsByDomainPrefix(String domainPrefix);

    /**
     * Finds a registration request by domain prefix.
     *
     * @param domainPrefix the domain prefix to search for
     * @return an Optional containing the request, or empty if not found
     */
    Optional<TenantRegistrationRequest> findByDomainPrefix(String domainPrefix);

    /**
     * Searches registration requests using multiple optional filter criteria.
     *
     * <p>All parameters are optional (nullable). Only non-null parameters are applied as filters.
     * {@code tenantBusinessName} supports partial/case-insensitive matching.
     * {@code contactNumber} is searched in both contactNumber1 and contactNumber2 fields.
     *
     * @param id                 optional filter by registration ID
     * @param packageCode        optional filter by exact package code
     * @param tenantBusinessName optional partial name search (case-insensitive)
     * @param approvalStatus     optional filter by approval status (P/A/R)
     * @param domainPrefix       optional filter by exact domain prefix
     * @param contactNumber      optional filter — searched in contactNumber1 AND contactNumber2
     * @param emailAddress       optional filter by exact email address
     * @return list of matching registration requests
     */
    @Query("SELECT r FROM TenantRegistrationRequest r WHERE " +
           "(:id IS NULL OR r.id = :id) AND " +
           "(:packageCode IS NULL OR r.packageCode = :packageCode) AND " +
           "(:tenantBusinessName IS NULL OR LOWER(r.tenantBusinessName) LIKE LOWER(CONCAT('%', :tenantBusinessName, '%'))) AND " +
           "(:approvalStatus IS NULL OR r.approvalStatus = :approvalStatus) AND " +
           "(:domainPrefix IS NULL OR r.domainPrefix = :domainPrefix) AND " +
           "(:contactNumber IS NULL OR r.contactNumber1 = :contactNumber OR r.contactNumber2 = :contactNumber) AND " +
           "(:emailAddress IS NULL OR r.emailAddress = :emailAddress) " +
           "ORDER BY r.createdAt DESC")
    List<TenantRegistrationRequest> searchRegistrations(
            @Param("id") Long id,
            @Param("packageCode") String packageCode,
            @Param("tenantBusinessName") String tenantBusinessName,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            @Param("domainPrefix") String domainPrefix,
            @Param("contactNumber") String contactNumber,
            @Param("emailAddress") String emailAddress);

    /**
     * Returns all registration requests ordered by submission date (newest first).
     *
     * @return all registration requests
     */
    List<TenantRegistrationRequest> findAllByOrderByCreatedAtDesc();
}
