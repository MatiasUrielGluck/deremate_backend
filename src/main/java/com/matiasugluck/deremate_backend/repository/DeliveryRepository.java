package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d JOIN d.route r WHERE r.assignedTo.id = :userId")
    List<Delivery> findByUserId(@Param("userId") Long userId);


    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Delivery d WHERE d.route.id = :routeId")
    boolean existsByRoute_Id(@Param("routeId") Long routeId);
}
