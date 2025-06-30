package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.Device;
import com.matiasugluck.deremate_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device,Long> {
}