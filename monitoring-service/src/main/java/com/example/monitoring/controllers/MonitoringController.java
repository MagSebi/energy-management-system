package com.example.monitoring.controllers;

import com.example.monitoring.entities.HourlyEnergyConsumption;
import com.example.monitoring.entities.DeviceMeasurement;
import com.example.monitoring.services.MonitoringService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/hourly-consumption")
    public ResponseEntity<List<HourlyEnergyConsumption>> getAllHourlyConsumption() {
        return ResponseEntity.ok(monitoringService.getAllHourlyConsumption());
    }

    @GetMapping("/hourly-consumption/device/{deviceId}")
    public ResponseEntity<List<HourlyEnergyConsumption>> getHourlyConsumptionByDevice(
            @PathVariable UUID deviceId) {
        return ResponseEntity.ok(monitoringService.getHourlyConsumptionByDevice(deviceId));
    }

    @GetMapping("/hourly-consumption/device/{deviceId}/range")
    public ResponseEntity<List<HourlyEnergyConsumption>> getHourlyConsumptionByDeviceAndDateRange(
            @PathVariable UUID deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(
                monitoringService.getHourlyConsumptionByDeviceAndDateRange(deviceId, start, end)
        );
    }

    @GetMapping("/hourly-consumption/device/{deviceId}/day")
    public ResponseEntity<List<HourlyEnergyConsumption>> getHourlyConsumptionByDeviceAndDay(
            @PathVariable UUID deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return ResponseEntity.ok(
                monitoringService.getHourlyConsumptionByDeviceAndDateRange(deviceId, startOfDay, endOfDay)
        );
    }

    @GetMapping("/measurements/device/{deviceId}/latest")
    public ResponseEntity<List<DeviceMeasurement>> getLatestMeasurements(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(monitoringService.getLatestMeasurements(deviceId, limit));
    }

    @GetMapping("/measurements/latest")
    public ResponseEntity<List<DeviceMeasurement>> getAllLatestMeasurements(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(monitoringService.getAllLatestMeasurements(limit));
    }
}

