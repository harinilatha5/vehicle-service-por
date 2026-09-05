package com.vehicleportal.repository;

import com.vehicleportal.model.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByVehicleId(Long vehicleId);
    List<ServiceRecord> findByStatus(ServiceRecord.ServiceStatus status);
}
