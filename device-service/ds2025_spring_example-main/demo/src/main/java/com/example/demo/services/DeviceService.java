package com.example.demo.services;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.messaging.SyncPublisher;
import com.example.demo.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final SyncPublisher syncPublisher;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, SyncPublisher syncPublisher) {
        this.deviceRepository = deviceRepository;
        this.syncPublisher = syncPublisher;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> list = deviceRepository.findAll();
        return list.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findById(UUID id) {
        Optional<Device> opt = deviceRepository.findById(id);
        if (opt.isEmpty()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(opt.get());
    }

    @Transactional
    public DeviceDetailsDTO update(UUID id, DeviceDetailsDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Device with id {} was not found in db", id);
                    return new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
                });

        // update câmpuri
        device.setName(dto.getName());
        device.setEnergyConsumption(dto.getEnergyConsumption());
        device.setDescription(dto.getDescription());
        device.setAssignedUserId(dto.getAssignedUserId());

        // Publish sync event
        syncPublisher.publishDeviceUpdated(device.getId(), device.getName(), device.getEnergyConsumption());

        // @Transactional gestionează persist/flush
        return DeviceBuilder.toDeviceDetailsDTO(device);
    }

    public void delete(UUID id) {
        if (!deviceRepository.existsById(id)) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        deviceRepository.deleteById(id);
        LOGGER.debug("Device with id {} was deleted from db", id);

        // Publish sync event
        syncPublisher.publishDeviceDeleted(id);
    }

    // new: find devices assigned to a user
    public List<DeviceDTO> findByAssignedUserId(UUID userId) {
        List<Device> list = deviceRepository.findByAssignedUserId(userId);
        return list.stream().map(DeviceBuilder::toDeviceDTO).collect(Collectors.toList());
    }

    public void assignDeviceToUser(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        UUID previousUserId = device.getAssignedUserId();
        device.setAssignedUserId(userId);
        Device saved = deviceRepository.save(device);

        // Publish assignment event
        if (userId != null) {
            syncPublisher.publishDeviceAssigned(deviceId, userId, device.getName());
        } else if (previousUserId != null) {
            syncPublisher.publishDeviceUnassigned(deviceId);
        }
    }

    public DeviceDetailsDTO createDevice(DeviceDetailsDTO dto) {
        Device device = new Device();
        device.setName(dto.getName());
        device.setEnergyConsumption(dto.getEnergyConsumption());
        device.setDescription(dto.getDescription());
        device.setAssignedUserId(dto.getAssignedUserId());

        Device saved = deviceRepository.save(device);

        // Publish sync event
        syncPublisher.publishDeviceCreated(saved.getId(), saved.getName(), saved.getEnergyConsumption());

        return DeviceBuilder.toDeviceDetailsDTO(saved);
    }


}
