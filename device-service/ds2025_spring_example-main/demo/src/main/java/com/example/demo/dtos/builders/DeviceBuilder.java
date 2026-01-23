package com.example.demo.dtos.builders;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {}

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(device.getId(), device.getName(), device.getEnergyConsumption());
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getEnergyConsumption(),
                device.getDescription(),
                device.getAssignedUserId()
        );
    }

    public static Device toEntity(DeviceDetailsDTO dto) {
        Device device = new Device(
                dto.getName(),
                dto.getEnergyConsumption(),
                dto.getDescription()
        );
        device.setAssignedUserId(dto.getAssignedUserId());
        return device;
    }
}
