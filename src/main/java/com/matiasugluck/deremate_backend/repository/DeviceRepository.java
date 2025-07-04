package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.Device;
import com.matiasugluck.deremate_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    void deleteByDeviceId(String deviceId);
    void deleteByUserAndDeviceId(User user, String deviceId);
}
