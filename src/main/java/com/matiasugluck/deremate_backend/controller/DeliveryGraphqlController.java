package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.graphql.input.DeliveryFilterInput; // Deberás crear esta clase
import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DeliveryGraphqlController {

    private final DeliveryService deliveryService;

    public DeliveryGraphqlController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }


    @QueryMapping
    public List<Delivery> deliveries(@Argument DeliveryFilterInput filter) {
        return deliveryService.findDeliveriesByFilter(filter);
    }
}
