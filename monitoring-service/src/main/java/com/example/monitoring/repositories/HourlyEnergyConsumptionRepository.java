package com.example.monitoring.repositories;

import com.example.monitoring.entities.HourlyEnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HourlyEnergyConsumptionRepository extends JpaRepository<HourlyEnergyConsumption, Long> {

    Optional<HourlyEnergyConsumption> findByDeviceIdAndHourTimestamp(UUID deviceId, LocalDateTime hourTimestamp);

    List<HourlyEnergyConsumption> findByDeviceId(UUID deviceId);

    List<HourlyEnergyConsumption> findByDeviceIdAndHourTimestampBetween(
            UUID deviceId,
            LocalDateTime start,
            LocalDateTime end
    );
}

