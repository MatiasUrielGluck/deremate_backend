package com.matiasugluck.deremate_backend;

import com.google.zxing.WriterException;
import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.repository.DeliveryRepository;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.utils.PinGenerator;
import com.matiasugluck.deremate_backend.utils.QRCodeGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;


import java.sql.Timestamp;
import java.time.LocalDateTime;


import java.io.IOException;

import java.util.Random;
import java.util.stream.IntStream;

@SpringBootApplication
public class DeremateBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeremateBackendApplication.class, args);
    }

    @Profile("!test")
    @Bean
    CommandLineRunner loadData(DeliveryRepository deliveryRepository, RouteRepository routeRepository) {
        return args -> {
            if (deliveryRepository.count() == 0) {
                String[] sectores = {"Sector A", "Sector B", "Sector C", "Sector D", "Sector E"};
                Random random = new Random();

                IntStream.rangeClosed(1, 10).forEach(i -> {
                    String sector = sectores[random.nextInt(sectores.length)];
                    int estante = 1 + random.nextInt(5);
                    String codigo = String.format("iVBORw0KGgoAAAANSUhEUgAAASwAAAEsAQAAAABRBrPYAAABLUlEQVR4Xu3WPW6EMBQEYG/FMXxUfFQfIeVWIZ555kdPK+1GSoZmpgDD+6hGBsr2Sb5KvvMyZilmKWYpZilmKWYpv2PPMlNj9djavPEw07G42lodq3m4DsxErLEiiP0Bdmd2CxvDspjdy45LszsYw+HaWdZ1YCZhZaY+2RO7Y8x07JLvc3jGLN38F4aNwsbqYNwyW18uD5hJ2N7OOcRq7WZChopwGu8sqFFWw3l0FzFTMJaFA9pp2DfzQ2KmZKxmDxuDaNw8ZiJ2hhUdDI0hZgp2lIUhL1cu+sKTmYZFJ9HTirdXfMTjrpmMNbykYo+gojbbi+fNtAw/VR3DVz+0Zkp2lFU5NZMxhluGtnBVFjMhYzVohwyXFU+hQDMVexezFLMUsxSzFLMUs5Q/Zj/qmgpsnUNaZwAAAABJRU5ErkJggg==", i);
                    String pin = String.format("%04d", random.nextInt(10000)); // pin fijo de 6 dígitos

                    Route route = Route.builder()
                            .origin("Depósito Central")
                            .destination("Destino Entrega " + i)
                            .status(RouteStatus.PENDING)
                            .build();
                    Route savedRoute = routeRepository.save(route);

                    Delivery d = Delivery.builder()
                            .status(DeliveryStatus.NOT_DELIVERED)
                            .packageLocation(sector + " - Estante " + estante)
                            .destination("Destino Entrega " + i)
                            .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                            .qrCode(codigo)
                            .pin(pin)
                            .route(savedRoute)
                            .build();

                    String qrCodeBase64 = null;
                    try {
                        qrCodeBase64 = QRCodeGenerator.generateQRCodeBase64((long) i);
                    } catch (WriterException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    d.setQrCode(qrCodeBase64);

                    deliveryRepository.save(d);
                });
            }
        };
    }
}
