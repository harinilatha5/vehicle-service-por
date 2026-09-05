package com.vehicleportal.controller;

import com.vehicleportal.model.Vehicle;
import com.vehicleportal.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/vehicles")
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @GetMapping("/vehicles/{id}")
    public Vehicle getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }

    @GetMapping("/customers/{customerId}/vehicles")
    public List<Vehicle> getVehiclesByCustomer(@PathVariable Long customerId) {
        return vehicleService.getVehiclesByCustomer(customerId);
    }

    @PostMapping("/customers/{customerId}/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle createVehicle(@PathVariable Long customerId, @Valid @RequestBody Vehicle vehicle) {
        return vehicleService.createVehicle(customerId, vehicle);
    }

    @PutMapping("/vehicles/{id}")
    public Vehicle updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        return vehicleService.updateVehicle(id, vehicle);
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
