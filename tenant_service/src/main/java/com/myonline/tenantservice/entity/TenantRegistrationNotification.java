package com.myonline.tenantservice.entity;

import com.myonline.tenantservice.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a notification queued after a tenant registration is approved or rejected.
 *
 * <p>Notifications start in {@link NotificationStatus#PENDING} status.
 * A future Notification Service will pick them up and deliver them via email/SMS,
 * then update the status to {@link NotificationStatus#SENT} or {@link NotificationStatus#FAILED}.
 *
 * <p>Mapped to the {@code tenant_registration_notification} table.
 */
@Entity
@Table(name = "tenant_registration_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRegistrationNotification {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    /** Email address of the registration requestor. */
    @Column(name = "email_address", nullable = false, length = 150)
    private String emailAddress;

    /** Primary contact phone number of the requestor. */
    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    /** Reason for rejection — populated only when the registration is rejected. */
    @Column(name = "reason_for_rejection", length = 500)
    private String reasonForRejection;

    /** Full notification message body to be delivered to the requestor. */
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Delivery status of the notification.
     * Defaults to {@link NotificationStatus#PENDING}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    /** Foreign key reference to the originating registration request. */
    @Column(name = "registration_request_id")
    private Long registrationRequestId;

    /** Timestamp when this notification was created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
