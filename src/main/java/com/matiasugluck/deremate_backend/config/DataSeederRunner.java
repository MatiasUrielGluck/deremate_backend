package com.matiasugluck.deremate_backend.config;

import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.Product;
import com.matiasugluck.deremate_backend.repository.DeliveryRepository;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.repository.ProductRepository;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.utils.QRCodeGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Configuration
public class DataSeederRunner {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      ProductRepository productRepository,
                                      RouteRepository routeRepository,
                                      DeliveryRepository deliveryRepository) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // 1. Crear usuarios hardcodeados
            User user1 = User.builder()
                    .firstname("Tomas")
                    .lastname("Admin")
                    .email("test@test.com")
                    .password(encoder.encode("Hola1234."))
                    .isEmailVerified(true)
                    .build();

            User user2 = User.builder()
                    .firstname("Tomas")
                    .lastname("Admin2")
                    .email("xxxxx")
                    .password(encoder.encode("lucia456"))
                    .isEmailVerified(true)
                    .build();

            userRepository.saveAll(List.of(user1, user2));
            System.out.println("✅ Usuarios hardcodeados creados.");

            // 2. Crear usuarios aleatorios
            List<User> randomUsers = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                User u = User.builder()
                        .firstname("User" + i)
                        .lastname("Test" + i)
                        .email("user" + i + "@test.com")
                        .password(encoder.encode("pwd" + i))
                        .isEmailVerified(true)
                        .build();
                randomUsers.add(u);
            }

            userRepository.saveAll(randomUsers);
            System.out.println("✅ Usuarios aleatorios creados: " + randomUsers.size());

            // 3. Crear productos
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                products.add(Product.builder()
                        .name("Producto " + i)
                        .description("Descripción producto " + i)
                        .price(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10, 500)))
                        .build());
            }
            productRepository.saveAll(products);
            System.out.println("✅ Productos creados: " + products.size());

            // 4. Crear rutas con estado asignado, completado o en progreso
            List<User> allUsers = userRepository.findAll();
            List<Route> routes = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                List<User> hardcodedUsers = List.of(user1, user2);
                User assignedUser = hardcodedUsers.get(ThreadLocalRandom.current().nextInt(hardcodedUsers.size()));
                RouteStatus routeStatus = RouteStatus.PENDING;
                if (i % 3 == 0) routeStatus = RouteStatus.INITIATED;
                else if (i % 3 == 1) routeStatus = RouteStatus.COMPLETED;

                LocalDateTime completedAt = (routeStatus == RouteStatus.COMPLETED)
                        ? LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(1, 10)) : null;

                routes.add(Route.builder()
                        .origin("Origen " + i)
                        .destination("Destino " + i)
                        .status(routeStatus)
                        .assignedTo(assignedUser)
                        .completedAt(completedAt != null ? Timestamp.valueOf(completedAt) : null)
                        .build());
            }
            routeRepository.saveAll(routes);
            System.out.println("✅ Rutas creadas y asignadas con estado aleatorio.");

            // 5. Crear entregas con sector y estante en packageLocation
            DeliveryStatus[] estados = DeliveryStatus.values();
            List<Delivery> deliveries = new ArrayList<>();
            String[] sectores = {"Sector A", "Sector B", "Sector C", "Sector D", "Sector E"};
            Random random = new Random();
            int routeIndex = 0;

            for (int i = 0; i < 15; i++) {
                Route route = routes.get(routeIndex);
                routeIndex = (routeIndex + 1) % routes.size();

                List<Product> randomProducts = ThreadLocalRandom.current().ints(3, 0, products.size())
                        .distinct()
                        .mapToObj(products::get)
                        .toList();

                DeliveryStatus estado = estados[i % estados.length];
                Timestamp createdDate = Timestamp.valueOf(LocalDateTime.now().minusDays(i));
                Timestamp deliveryStartDate = Timestamp.valueOf(LocalDateTime.now().minusDays(i + 1));
                Timestamp deliveryEndDate = estado == DeliveryStatus.DELIVERED ? Timestamp.valueOf(LocalDateTime.now()) : null;

                String sector = sectores[random.nextInt(sectores.length)];
                int estante = 1 + random.nextInt(5);
                String location = sector + " - Estante " + estante;

                Delivery delivery = Delivery.builder()
                        .status(estado)
                        .destination("Destino Entrega " + i)
                        .packageLocation(location)
                        .createdDate(createdDate)
                        .deliveryStartDate(deliveryStartDate)
                        .deliveryEndDate(deliveryEndDate)
                        .pin(String.format("%04d", ThreadLocalRandom.current().nextInt(10000)))
                        .products(randomProducts)
                        .route(route)
                        .build();

                deliveries.add(delivery);
            }

            deliveryRepository.saveAll(deliveries);

            for (Delivery delivery : deliveries) {
                String qrCodeBase64 = QRCodeGenerator.generateQRCodeBase64(delivery.getId());
                delivery.setQrCode(qrCodeBase64);
            }

            deliveryRepository.saveAll(deliveries);
            System.out.println("✅ Entregas creadas: " + deliveries.size());

            Map<DeliveryStatus, Long> cantidadPorEstado = deliveries.stream()
                    .collect(Collectors.groupingBy(Delivery::getStatus, Collectors.counting()));

            System.out.println("\n📦 Resumen de entregas por estado:");
            cantidadPorEstado.forEach((estado, cantidad) ->
                    System.out.println(" - " + estado + ": " + cantidad + " entregas"));
        };
    }
}