package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findAllByUserId(Long userId);
}
