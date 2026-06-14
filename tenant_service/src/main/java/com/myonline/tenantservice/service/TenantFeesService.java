package com.myonline.tenantservice.service;

import com.myonline.tenantservice.dto.request.CreateTenantFeesRequest;
import com.myonline.tenantservice.dto.request.UpdateTenantFeesRequest;
import com.myonline.tenantservice.dto.response.TenantFeesResponse;
import com.myonline.tenantservice.entity.TenantFees;
import com.myonline.tenantservice.exception.DuplicateResourceException;
import com.myonline.tenantservice.exception.ResourceNotFoundException;
import com.myonline.tenantservice.repository.TenantFeesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing tenant subscription packages (tenant_fees).
 *
 * <p>Business rules enforced:
 * <ul>
 *   <li>Package codes must be unique.</li>
 *   <li>Product count ranges must not overlap with existing packages.</li>
 *   <li>productCountFrom must be less than productCountTo.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantFeesService {

    private final TenantFeesRepository tenantFeesRepository;

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    /**
     * Creates a new tenant subscription package.
     *
     * @param request the package details
     * @return the created package as a response DTO
     * @throws DuplicateResourceException if a package with the same code already exists
     * @throws IllegalArgumentException   if productCountFrom >= productCountTo or range overlaps
     */
    @Transactional
    public TenantFeesResponse createTenantFees(CreateTenantFeesRequest request) {
        log.debug("Creating tenant fees package: {}", request.getPackageCode());

        // Check for duplicate package code
        if (tenantFeesRepository.existsByPackageCode(request.getPackageCode())) {
            throw new DuplicateResourceException("TenantFees", "packageCode", request.getPackageCode());
        }

        // Validate range order
        validateRangeOrder(request.getProductCountFrom(), request.getProductCountTo());

        // Check for overlapping ranges with existing packages
        List<TenantFees> overlapping = tenantFeesRepository.findOverlappingRanges(
                request.getProductCountFrom(), request.getProductCountTo());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Product count range [%d, %d] overlaps with existing package: '%s' [%d, %d]",
                            request.getProductCountFrom(), request.getProductCountTo(),
                            overlapping.get(0).getPackageCode(),
                            overlapping.get(0).getProductCountFrom(),
                            overlapping.get(0).getProductCountTo()));
        }

        TenantFees tenantFees = TenantFees.builder()
                .packageCode(request.getPackageCode().toUpperCase())
                .packageName(request.getPackageName())
                .productCountFrom(request.getProductCountFrom())
                .productCountTo(request.getProductCountTo())
                .registrationFee(request.getRegistrationFee())
                .monthlyFee(request.getMonthlyFee())
                .build();

        TenantFees saved = tenantFeesRepository.save(tenantFees);
        log.info("Tenant fees package created: {}", saved.getPackageCode());
        return toResponse(saved);
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    /**
     * Returns all tenant subscription packages ordered by productCountFrom.
     *
     * @return list of all packages as response DTOs
     */
    public List<TenantFeesResponse> getAllTenantFees() {
        log.debug("Fetching all tenant fees packages");
        return tenantFeesRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getProductCountFrom(), b.getProductCountFrom()))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns a single package by its package code.
     *
     * @param packageCode the package code to look up
     * @return the package as a response DTO
     * @throws ResourceNotFoundException if no package with the given code exists
     */
    public TenantFeesResponse getTenantFeesByPackageCode(String packageCode) {
        log.debug("Fetching tenant fees package: {}", packageCode);
        TenantFees tenantFees = findByPackageCodeOrThrow(packageCode);
        return toResponse(tenantFees);
    }

    /**
     * Searches and lists packages by product count range filter.
     *
     * <p>Both parameters are optional. If provided:
     * <ul>
     *   <li>{@code from} — filters packages where productCountFrom >= from</li>
     *   <li>{@code to} — filters packages where productCountTo <= to</li>
     * </ul>
     * Results are ordered by productCountFrom ascending.
     *
     * @param from optional lower bound filter
     * @param to   optional upper bound filter
     * @return list of matching packages
     */
    public List<TenantFeesResponse> searchByProductCountRange(Integer from, Integer to) {
        log.debug("Searching tenant fees by product count range: from={}, to={}", from, to);
        return tenantFeesRepository.findByProductCountRange(from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    /**
     * Updates an existing tenant subscription package.
     * Only non-null fields in the request are applied.
     *
     * @param packageCode the code of the package to update
     * @param request     the fields to update
     * @return the updated package as a response DTO
     * @throws ResourceNotFoundException if no package with the given code exists
     * @throws IllegalArgumentException  if the updated range is invalid or overlaps
     */
    @Transactional
    public TenantFeesResponse updateTenantFees(String packageCode, UpdateTenantFeesRequest request) {
        log.debug("Updating tenant fees package: {}", packageCode);
        TenantFees tenantFees = findByPackageCodeOrThrow(packageCode);

        // Apply partial updates
        if (request.getPackageName() != null) {
            tenantFees.setPackageName(request.getPackageName());
        }

        Integer newFrom = request.getProductCountFrom() != null
                ? request.getProductCountFrom() : tenantFees.getProductCountFrom();
        Integer newTo = request.getProductCountTo() != null
                ? request.getProductCountTo() : tenantFees.getProductCountTo();

        // If range fields were updated, validate them
        if (request.getProductCountFrom() != null || request.getProductCountTo() != null) {
            validateRangeOrder(newFrom, newTo);

            // Check for overlapping ranges (exclude this package from the check)
            List<TenantFees> overlapping = tenantFeesRepository.findOverlappingRangesExcluding(
                    newFrom, newTo, packageCode);
            if (!overlapping.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("Product count range [%d, %d] overlaps with existing package: '%s' [%d, %d]",
                                newFrom, newTo,
                                overlapping.get(0).getPackageCode(),
                                overlapping.get(0).getProductCountFrom(),
                                overlapping.get(0).getProductCountTo()));
            }

            tenantFees.setProductCountFrom(newFrom);
            tenantFees.setProductCountTo(newTo);
        }

        if (request.getRegistrationFee() != null) {
            tenantFees.setRegistrationFee(request.getRegistrationFee());
        }
        if (request.getMonthlyFee() != null) {
            tenantFees.setMonthlyFee(request.getMonthlyFee());
        }

        TenantFees updated = tenantFeesRepository.save(tenantFees);
        log.info("Tenant fees package updated: {}", updated.getPackageCode());
        return toResponse(updated);
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    /**
     * Deletes a tenant subscription package by its package code.
     *
     * @param packageCode the code of the package to delete
     * @throws ResourceNotFoundException if no package with the given code exists
     */
    @Transactional
    public void deleteTenantFees(String packageCode) {
        log.debug("Deleting tenant fees package: {}", packageCode);
        TenantFees tenantFees = findByPackageCodeOrThrow(packageCode);
        tenantFeesRepository.delete(tenantFees);
        log.info("Tenant fees package deleted: {}", packageCode);
    }

    // ------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------

    /**
     * Finds a TenantFees entity by packageCode or throws ResourceNotFoundException.
     *
     * @param packageCode the package code
     * @return the found entity
     */
    public TenantFees findByPackageCodeOrThrow(String packageCode) {
        return tenantFeesRepository.findById(packageCode)
                .orElseThrow(() -> new ResourceNotFoundException("TenantFees", "packageCode", packageCode));
    }

    /**
     * Validates that productCountFrom is strictly less than productCountTo.
     *
     * @param from the lower bound
     * @param to   the upper bound
     * @throws IllegalArgumentException if from >= to
     */
    private void validateRangeOrder(Integer from, Integer to) {
        if (from >= to) {
            throw new IllegalArgumentException(
                    String.format("productCountFrom (%d) must be less than productCountTo (%d)", from, to));
        }
    }

    /**
     * Maps a {@link TenantFees} entity to a {@link TenantFeesResponse} DTO.
     *
     * @param tenantFees the entity to map
     * @return the response DTO
     */
    public TenantFeesResponse toResponse(TenantFees tenantFees) {
        return TenantFeesResponse.builder()
                .packageCode(tenantFees.getPackageCode())
                .packageName(tenantFees.getPackageName())
                .productCountFrom(tenantFees.getProductCountFrom())
                .productCountTo(tenantFees.getProductCountTo())
                .registrationFee(tenantFees.getRegistrationFee())
                .monthlyFee(tenantFees.getMonthlyFee())
                .createdAt(tenantFees.getCreatedAt())
                .updatedAt(tenantFees.getUpdatedAt())
                .build();
    }
}
