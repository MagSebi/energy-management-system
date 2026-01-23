package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "energyConsumption is required")
    @Positive(message = "energyConsumption must be > 0")
    private Double energyConsumption;

    private String description;

    private UUID assignedUserId;

    public DeviceDetailsDTO() {}

    public DeviceDetailsDTO(String name, Double energyConsumption, String description) {
        this.name = name;
        this.energyConsumption = energyConsumption;
        this.description = description;
    }

    public DeviceDetailsDTO(UUID id, String name, Double energyConsumption,
                            String description, UUID assignedUserId) {
        this.id = id;
        this.name = name;
        this.energyConsumption = energyConsumption;
        this.description = description;
        this.assignedUserId = assignedUserId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getEnergyConsumption() { return energyConsumption; }
    public void setEnergyConsumption(Double energyConsumption) { this.energyConsumption = energyConsumption; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(UUID assignedUserId) { this.assignedUserId = assignedUserId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDetailsDTO that = (DeviceDetailsDTO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(energyConsumption, that.energyConsumption) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, energyConsumption, description);
    }
}
