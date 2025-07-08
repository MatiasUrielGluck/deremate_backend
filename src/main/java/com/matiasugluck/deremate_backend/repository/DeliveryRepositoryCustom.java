package com.matiasugluck.deremate_backend.repository;
import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.graphql.input.DeliveryFilterInput;
import java.util.List;

public interface DeliveryRepositoryCustom {
    List<Delivery> findDeliveriesByFilter(DeliveryFilterInput filter);
}
