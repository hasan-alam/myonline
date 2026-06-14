package com.myonline.tenantservice.repository;

import com.myonline.tenantservice.entity.TenantFees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link TenantFees} entity.
 * Provides CRUD operations and custom queries for tenant subscription packages.
 */
@Repository
public interface TenantFeesRepository extends JpaRepository<TenantFees, String> {

    /**
     * Checks if a package with the given code already exists.
     *
     * @param packageCode the package code to check
     * @return true if a package with this code exists
     */
    boolean existsByPackageCode(String packageCode);

    /**
     * Finds all packages where the product count range overlaps with the given range.
     *
     * <p>Two ranges [a, b] and [c, d] overlap if: a <= d AND c <= b.
     * Used to validate that a new package's range does not conflict with existing ones.
     *
     * @param productCountTo   the upper bound of the range to check against
     * @param productCountFrom the lower bound of the range to check against
     * @return list of packages whose ranges overlap with [productCountFrom, productCountTo]
     */
    @Query("SELECT tf FROM TenantFees tf " +
           "WHERE tf.productCountFrom <= :productCountTo " +
           "AND tf.productCountTo >= :productCountFrom")
    List<TenantFees> findOverlappingRanges(@Param("productCountFrom") Integer productCountFrom,
                                            @Param("productCountTo") Integer productCountTo);

    /**
     * Finds all packages where the product count range overlaps, excluding a specific package.
     * Used during update validation to exclude the package being updated from overlap checks.
     *
     * @param productCountFrom the lower bound of the range to check
     * @param productCountTo   the upper bound of the range to check
     * @param excludeCode      the package code to exclude from the check
     * @return list of conflicting packages (excluding the one being updated)
     */
    @Query("SELECT tf FROM TenantFees tf " +
           "WHERE tf.productCountFrom <= :productCountTo " +
           "AND tf.productCountTo >= :productCountFrom " +
           "AND tf.packageCode <> :excludeCode")
    List<TenantFees> findOverlappingRangesExcluding(@Param("productCountFrom") Integer productCountFrom,
                                                     @Param("productCountTo") Integer productCountTo,
                                                     @Param("excludeCode") String excludeCode);

    /**
     * Finds the subscription package that covers a given product count.
     * Returns the package where productCountFrom <= productCount <= productCountTo.
     *
     * @param productCount the product count to find a package for
     * @return list of matching packages (should be at most one if ranges are non-overlapping)
     */
    @Query("SELECT tf FROM TenantFees tf " +
           "WHERE tf.productCountFrom <= :productCount " +
           "AND tf.productCountTo >= :productCount")
    List<TenantFees> findByProductCountInRange(@Param("productCount") Integer productCount);

    /**
     * Finds packages filtered by a product count range (from and/or to bounds).
     * Useful for search/filter by productCountFrom and productCountTo query params.
     *
     * @param from lower bound filter (nullable)
     * @param to   upper bound filter (nullable)
     * @return list of matching packages
     */
    @Query("SELECT tf FROM TenantFees tf " +
           "WHERE (:from IS NULL OR tf.productCountFrom >= :from) " +
           "AND (:to IS NULL OR tf.productCountTo <= :to) " +
           "ORDER BY tf.productCountFrom ASC")
    List<TenantFees> findByProductCountRange(@Param("from") Integer from,
                                              @Param("to") Integer to);
}
