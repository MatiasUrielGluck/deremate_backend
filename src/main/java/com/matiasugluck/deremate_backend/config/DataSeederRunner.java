package com.matiasugluck.deremate_backend.config;

import com.matiasugluck.deremate_backend.entity.*;
import com.matiasugluck.deremate_backend.repository.DeliveryRepository;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.repository.ProductRepository;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.utils.QRCodeGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    @Getter
    @AllArgsConstructor
    private static class LocationData {
        private final String address;
        private final Coordinates coords;
    }

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
                    .email("deberardistomas@gmail.com")
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

            // 4. <<< CAMBIO PRINCIPAL: Crear rutas con datos reales de CABA >>>

            // Lista de ubicaciones reales en CABA para usar en las rutas
            List<LocationData> cabaLocations = List.of(
                    new LocationData("Obelisco, Av. 9 de Julio s/n, CABA", new Coordinates(-34.6037, -58.3816)),
                    new LocationData("Plaza de Mayo, Bolívar 65, CABA", new Coordinates(-34.6083, -58.3722)),
                    new LocationData("Museo MALBA, Av. Figueroa Alcorta 3415, CABA", new Coordinates(-34.5801, -58.4063)),
                    new LocationData("Congreso de la Nación, Av. Rivadavia 1864, CABA", new Coordinates(-34.6096, -58.3925)),
                    new LocationData("Estadio Monumental, Av. Pres. Figueroa Alcorta 7597, CABA", new Coordinates(-34.5453, -58.4497)),
                    new LocationData("Caminito, La Boca, CABA", new Coordinates(-34.6383, -58.3633)),
                    new LocationData("Jardín Japonés, Av. Casares 2966, CABA", new Coordinates(-34.5770, -58.4121)),
                    new LocationData("Teatro Colón, Cerrito 628, CABA", new Coordinates(-34.6010, -58.3831)),
                    new LocationData("Puente de la Mujer, Puerto Madero, CABA", new Coordinates(-34.6094, -58.3640))
            );

            List<Route> routes = new ArrayList<>();
            List<User> hardcodedUsers = List.of(user1, user2); // Solo asignamos a los admins

            for (int i = 0; i < 15; i++) {
                // Seleccionamos una ubicación de la lista de forma cíclica
                LocationData location = cabaLocations.get(i % cabaLocations.size());

                // Tu lógica original para asignar usuario y estado
                User assignedUser = hardcodedUsers.get(ThreadLocalRandom.current().nextInt(hardcodedUsers.size()));
                RouteStatus routeStatus = RouteStatus.PENDING;
                if (i % 3 == 0) routeStatus = RouteStatus.INITIATED;
                else if (i % 3 == 1) routeStatus = RouteStatus.COMPLETED;

                LocalDateTime completedAt = (routeStatus == RouteStatus.COMPLETED)
                        ? LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(1, 10)) : null;

                routes.add(Route.builder()
                        .description(location.getAddress()) // Usamos la descripción textual
                        .destination(location.getCoords())  // Usamos el objeto Coordinates
                        .status(routeStatus)
                        .assignedTo(assignedUser)
                        .completedAt(completedAt != null ? Timestamp.valueOf(completedAt) : null)
                        .build());
            }
            routeRepository.saveAll(routes);
            System.out.println("✅ Rutas realistas creadas y asignadas.");


            // 5. Crear entregas (con una pequeña mejora para mayor coherencia)
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

                // <<< MEJORA >>> Hacemos que el destino de la entrega sea coherente con la ruta
                String deliveryDestination = "Oficina/Depto " + (i+1) + " en " + route.getDescription();

                Delivery delivery = Delivery.builder()
                        .status(estado)
                        .destination(deliveryDestination) // Usamos el destino mejorado
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