package com.matiasugluck.deremate_backend;

import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.repository.DeliveryRepository;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.utils.PinGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


import java.util.Random;
import java.util.stream.IntStream;

@SpringBootApplication
public class DeremateBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeremateBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(DeliveryRepository deliveryRepository, RouteRepository routeRepository) {
        return args -> {
            if (deliveryRepository.count() == 0) {
                String[] sectores = {"Sector A", "Sector B", "Sector C", "Sector D", "Sector E"};
                Random random = new Random();

                IntStream.rangeClosed(1, 10).forEach(i -> {
                    String sector = sectores[random.nextInt(sectores.length)];
                    int estante = 1 + random.nextInt(5);
                    String codigo = String.format("PKG%03d", i);
                    String pin = PinGenerator.generatePin();

                    // 1. Crear y guardar Route primero
                    Route route = Route.builder()
                            .origin("Depósito Central")
                            .destination("Destino " + i)
                            .status(RouteStatus.PENDING)
                            .build();
                    Route savedRoute = routeRepository.save(route);

                    // 2. Crear y guardar Delivery con Route asociada
                    Delivery d = Delivery.builder()
                            .status(DeliveryStatus.NOT_DELIVERED)
                            .packageLocation(sector + " - Estante " + estante)
                            .destination("Destino " + i)
                            .qrCode(codigo)
                            .pin(pin)
                            .route(savedRoute)
                            .build();

                    deliveryRepository.save(d);
                });
            }
        };
    }

}
