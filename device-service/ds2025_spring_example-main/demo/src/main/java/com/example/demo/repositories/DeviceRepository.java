package com.example.demo.repositories;

import com.example.demo.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    /**
     * Example: JPA generate query by existing field
     */
    List<Device> findByName(String name);

    /**
     * Example: Custom query (analog seniors≥60),
     * aici: "high consumption" definit ca energyConsumption ≥ 100 (schimbă pragul după nevoie)
     */
    @Query("SELECT d FROM Device d WHERE d.name = :name AND d.energyConsumption >= 100")
    Optional<Device> findHighConsumptionByName(@Param("name") String name);

    /**
     * Util pentru maparea device → user
     */
    List<Device> findByAssignedUserId(UUID assignedUserId);
}
