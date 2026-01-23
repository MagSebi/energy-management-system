package com.example.monitoring.repositories;

import com.example.monitoring.entities.DeviceMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceMeasurementRepository extends JpaRepository<DeviceMeasurement, Long> {

    List<DeviceMeasurement> findByDeviceIdAndTimestampBetween(
            UUID deviceId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<DeviceMeasurement> findByDeviceIdOrderByTimestampDesc(UUID deviceId);

    List<DeviceMeasurement> findAllByOrderByTimestampDesc();
}

