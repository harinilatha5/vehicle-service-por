package com.vehicleportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "service_records")
@Getter
@Setter
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @NotBlank(message = "Service type is required")
    private String serviceType; // OIL_CHANGE, TYRE_REPLACEMENT, GENERAL_CHECKUP etc.

    private String description;

    private LocalDate serviceDate;

    private Double cost;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status = ServiceStatus.PENDING;

    public enum ServiceStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
