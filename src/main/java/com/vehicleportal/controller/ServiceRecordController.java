package com.vehicleportal.controller;

import com.vehicleportal.model.ServiceRecord;
import com.vehicleportal.service.ServiceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

    @GetMapping("/services")
    public List<ServiceRecord> getAllServiceRecords() {
        return serviceRecordService.getAllServiceRecords();
    }

    @GetMapping("/services/{id}")
    public ServiceRecord getServiceRecordById(@PathVariable Long id) {
        return serviceRecordService.getServiceRecordById(id);
    }

    @GetMapping("/vehicles/{vehicleId}/services")
    public List<ServiceRecord> getServiceRecordsByVehicle(@PathVariable Long vehicleId) {
        return serviceRecordService.getServiceRecordsByVehicle(vehicleId);
    }

    @PostMapping("/vehicles/{vehicleId}/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRecord createServiceRecord(@PathVariable Long vehicleId, @Valid @RequestBody ServiceRecord record) {
        return serviceRecordService.createServiceRecord(vehicleId, record);
    }

    @PatchMapping("/services/{id}/status")
    public ServiceRecord updateStatus(@PathVariable Long id, @RequestParam ServiceRecord.ServiceStatus status) {
        return serviceRecordService.updateStatus(id, status);
    }

    @PutMapping("/services/{id}")
    public ServiceRecord updateServiceRecord(@PathVariable Long id, @Valid @RequestBody ServiceRecord record) {
        return serviceRecordService.updateServiceRecord(id, record);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteServiceRecord(@PathVariable Long id) {
        serviceRecordService.deleteServiceRecord(id);
        return ResponseEntity.noContent().build();
    }
}
