package com.example.monitoring.services;

import com.example.monitoring.entities.HourlyEnergyConsumption;
import com.example.monitoring.entities.DeviceMeasurement;
import com.example.monitoring.repositories.HourlyEnergyConsumptionRepository;
import com.example.monitoring.repositories.DeviceMeasurementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MonitoringService {

    private final HourlyEnergyConsumptionRepository hourlyConsumptionRepository;
    private final DeviceMeasurementRepository deviceMeasurementRepository;

    public MonitoringService(HourlyEnergyConsumptionRepository hourlyConsumptionRepository,
                             DeviceMeasurementRepository deviceMeasurementRepository) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.deviceMeasurementRepository = deviceMeasurementRepository;
    }

    public List<HourlyEnergyConsumption> getHourlyConsumptionByDevice(UUID deviceId) {
        return hourlyConsumptionRepository.findByDeviceId(deviceId);
    }

    public List<HourlyEnergyConsumption> getHourlyConsumptionByDeviceAndDateRange(
            UUID deviceId,
            LocalDateTime start,
            LocalDateTime end) {
        return hourlyConsumptionRepository.findByDeviceIdAndHourTimestampBetween(deviceId, start, end);
    }

    public List<HourlyEnergyConsumption> getAllHourlyConsumption() {
        return hourlyConsumptionRepository.findAll();
    }

    public List<DeviceMeasurement> getLatestMeasurements(UUID deviceId, int limit) {
        return deviceMeasurementRepository.findByDeviceIdOrderByTimestampDesc(deviceId)
            .stream()
            .limit(limit)
            .toList();
    }

    public List<DeviceMeasurement> getAllLatestMeasurements(int limit) {
        return deviceMeasurementRepository.findAllByOrderByTimestampDesc()
            .stream()
            .limit(limit)
            .toList();
    }
}

