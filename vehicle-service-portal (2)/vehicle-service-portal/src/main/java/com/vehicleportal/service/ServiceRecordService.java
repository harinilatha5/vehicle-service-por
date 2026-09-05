package com.vehicleportal.service;

import com.vehicleportal.exception.ResourceNotFoundException;
import com.vehicleportal.model.ServiceRecord;
import com.vehicleportal.model.Vehicle;
import com.vehicleportal.repository.ServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final VehicleService vehicleService;

    public List<ServiceRecord> getAllServiceRecords() {
        return serviceRecordRepository.findAll();
    }

    public ServiceRecord getServiceRecordById(Long id) {
        return serviceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service record not found with id: " + id));
    }

    public List<ServiceRecord> getServiceRecordsByVehicle(Long vehicleId) {
        return serviceRecordRepository.findByVehicleId(vehicleId);
    }

    public ServiceRecord createServiceRecord(Long vehicleId, ServiceRecord record) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        record.setVehicle(vehicle);
        return serviceRecordRepository.save(record);
    }

    public ServiceRecord updateStatus(Long id, ServiceRecord.ServiceStatus status) {
        ServiceRecord record = getServiceRecordById(id);
        record.setStatus(status);
        return serviceRecordRepository.save(record);
    }

    public ServiceRecord updateServiceRecord(Long id, ServiceRecord updated) {
        ServiceRecord existing = getServiceRecordById(id);
        existing.setServiceType(updated.getServiceType());
        existing.setDescription(updated.getDescription());
        existing.setServiceDate(updated.getServiceDate());
        existing.setCost(updated.getCost());
        existing.setStatus(updated.getStatus());
        return serviceRecordRepository.save(existing);
    }

    public void deleteServiceRecord(Long id) {
        ServiceRecord existing = getServiceRecordById(id);
        serviceRecordRepository.delete(existing);
    }
}
