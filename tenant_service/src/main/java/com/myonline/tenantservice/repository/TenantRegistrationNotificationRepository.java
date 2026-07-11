package com.myonline.tenantservice.repository;

import com.myonline.tenantservice.entity.TenantRegistrationNotification;
import com.myonline.tenantservice.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link TenantRegistrationNotification} entities.
 */
@Repository
public interface TenantRegistrationNotificationRepository
        extends JpaRepository<TenantRegistrationNotification, Long> {

    List<TenantRegistrationNotification> findByStatus(NotificationStatus status);

    List<TenantRegistrationNotification> findByRegistrationRequestId(Long registrationRequestId);
}
