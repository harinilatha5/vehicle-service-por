package com.vehicleportal.service;

import com.vehicleportal.exception.ResourceNotFoundException;
import com.vehicleportal.model.Customer;
import com.vehicleportal.model.Vehicle;
import com.vehicleportal.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    public List<Vehicle> getVehiclesByCustomer(Long customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }

    public Vehicle createVehicle(Long customerId, Vehicle vehicle) {
        Customer customer = customerService.getCustomerById(customerId);
        vehicle.setCustomer(customer);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Long id, Vehicle updated) {
        Vehicle existing = getVehicleById(id);
        existing.setMake(updated.getMake());
        existing.setModel(updated.getModel());
        existing.setYear(updated.getYear());
        existing.setVehicleType(updated.getVehicleType());
        existing.setRegistrationNumber(updated.getRegistrationNumber());
        return vehicleRepository.save(existing);
    }

    public void deleteVehicle(Long id) {
        Vehicle existing = getVehicleById(id);
        vehicleRepository.delete(existing);
    }
}
