package com.example.demo.dtos;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private Double energyConsumption;

    public DeviceDTO() {}
    public DeviceDTO(UUID id, String name, Double energyConsumption) {
        this.id = id; this.name = name; this.energyConsumption = energyConsumption;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getEnergyConsumption() { return energyConsumption; }
    public void setEnergyConsumption(Double energyConsumption) { this.energyConsumption = energyConsumption; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDTO that = (DeviceDTO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(energyConsumption, that.energyConsumption);
    }
    @Override public int hashCode() { return Objects.hash(name, energyConsumption); }
}
