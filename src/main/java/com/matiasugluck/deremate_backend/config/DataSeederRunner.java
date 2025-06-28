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
			// 1. Crear usuarios hardcodeados
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			User user1 = User.builder()
							.firstname("Test")
							.lastname("Uno")
							.email("user1@test.com")
							.password(encoder.encode("pwd1"))
							.isEmailVerified(true)
							.build();

			User user2 = User.builder()
							.firstname("Tomas")
							.lastname("Admin")
							.email("deberardistomas@gmail.com")
							.password(encoder.encode("Hola1234."))
							.isEmailVerified(true)
							.build();

			userRepository.saveAll(List.of(user1, user2));
			System.out.println("✅ Usuarios actualizados con user1@test.com");

// Crear rutas específicas para user1
			Route completedRoute1 = Route.builder()
							.description("Entrega completada 1 - CABA")
							.destination(new Coordinates(-34.60, -58.38))
							.status(RouteStatus.COMPLETED)
							.startedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(3)))
							.completedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
							.assignedTo(user1)
							.build();

			Route completedRoute2 = Route.builder()
							.description("Entrega completada 2 - CABA")
							.destination(new Coordinates(-34.61, -58.39))
							.status(RouteStatus.COMPLETED)
							.startedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(5)))
							.completedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(2)))
							.assignedTo(user1)
							.build();

// Ruta PENDING sin usuario asignado, para delivery ID = 1
			Route availableRoute = Route.builder()
							.description("Plaza de Mayo, Bolívar 65, CABA")
							.destination(new Coordinates(-34.62, -58.40))
							.status(RouteStatus.PENDING)
							.build();

			List<Route> savedRoutes = routeRepository.saveAll(List.of(availableRoute, completedRoute1, completedRoute2));

// Crear productos si no existían aún
			if (productRepository.count() == 0) {
				List<Product> products = new ArrayList<>();
				for (int i = 0; i < 20; i++) {
					products.add(Product.builder()
									.name("Producto " + i)
									.description("Descripción producto " + i)
									.price(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10, 500)))
									.build());
				}
				productRepository.saveAll(products);
				System.out.println("✅ Productos creados.");
			}

			List<Product> allProducts = productRepository.findAll();
			List<Product> someProducts = allProducts.subList(0, Math.min(3, allProducts.size()));

// Entregas completadas para user1
			Delivery delivered1 = Delivery.builder()
							.status(DeliveryStatus.DELIVERED)
							.destination("Palermo")
							.packageLocation("Sector A - Estante 2")
							.createdDate(Timestamp.valueOf(LocalDateTime.now().minusDays(3)))
							.deliveryStartDate(Timestamp.valueOf(LocalDateTime.now().minusDays(2)))
							.deliveryEndDate(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
							.pin("1234")
							.route(savedRoutes.get(1))
							.products(someProducts)
							.build();

			Delivery delivered2 = Delivery.builder()
							.status(DeliveryStatus.DELIVERED)
							.destination("Villa Urquiza")
							.packageLocation("Sector B - Estante 1")
							.createdDate(Timestamp.valueOf(LocalDateTime.now().minusDays(4)))
							.deliveryStartDate(Timestamp.valueOf(LocalDateTime.now().minusDays(3)))
							.deliveryEndDate(Timestamp.valueOf(LocalDateTime.now().minusDays(2)))
							.pin("5678")
							.route(savedRoutes.get(2))
							.products(someProducts)
							.build();

// Delivery disponible con ID = 1
			Delivery deliveryAvailable = Delivery.builder()
							.status(DeliveryStatus.NOT_DELIVERED)
							.destination("Villa Crespo")
							.packageLocation("Sector C - Estante 1")
							.createdDate(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
							.deliveryStartDate(null)
							.deliveryEndDate(null)
							.pin("9516")
							.route(savedRoutes.get(0))
							.products(someProducts)
							.build();

      List<Delivery> savedDeliveries = deliveryRepository.saveAll(List.of(deliveryAvailable, delivered1, delivered2));

// Generar códigos QR
			for (Delivery d : savedDeliveries) {
				String qr = QRCodeGenerator.generateQRCodeBase64(d.getId());
				d.setQrCode(qr);
			}
			deliveryRepository.saveAll(savedDeliveries);

			System.out.println("✅ Se configuraron los casos de uso de login, entregas completadas y entrega disponible.");

			// ➕ Crear 6 usuarios adicionales: user2@test.com hasta user7@test.com
			List<User> extraUsers = new ArrayList<>();
			for (int i = 2; i <= 7; i++) {
				User user = User.builder()
								.firstname("Test")
								.lastname("User" + i)
								.email("user" + i + "@test.com")
								.password(encoder.encode("pwd" + i))
								.isEmailVerified(true)
								.build();
				extraUsers.add(user);
			}
			List<User> savedExtraUsers = userRepository.saveAll(extraUsers);
			System.out.println("✅ Usuarios extra creados: " + savedExtraUsers.size());

// ➕ Crear 10 rutas nuevas (algunas completadas, otras PENDING sin usuario asignado)
			List<Route> newRoutes = new ArrayList<>();
			List<Route> completedRoutes = new ArrayList<>();
			List<Route> pendingRoutes = new ArrayList<>();

			List<String> addresses = List.of(
							"Obelisco, Av. 9 de Julio s/n, CABA",
							"Museo MALBA, Av. Figueroa Alcorta 3415, CABA",
							"Congreso de la Nación, Av. Rivadavia 1864, CABA",
							"Estadio Monumental, Av. Pres. Figueroa Alcorta 7597, CABA",
							"Jardín Japonés, Av. Casares 2966, CABA",
							"Teatro Colón, Cerrito 628, CABA",
							"Puente de la Mujer, Puerto Madero, CABA",
							"Caminito, La Boca, CABA",
							"Corrientes 4150, CABA",
							"Abasto, Corrientes 2450, CABA",
							"Malabia 1300, CABA"
			);

			for (int i = 0; i < 10; i++) {
				boolean isCompleted = i % 2 == 0;
				Route.RouteBuilder builder = Route.builder()
								.description(addresses.get(i))
								.destination(new Coordinates(-34.55 + i * 0.01, -58.45 + i * 0.01))
								.status(isCompleted ? RouteStatus.COMPLETED : RouteStatus.PENDING);

				if (isCompleted) {
					User assignedUser = savedExtraUsers.get(i % savedExtraUsers.size());
					builder.assignedTo(assignedUser)
									.startedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(10 - i)))
									.completedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(9 - i)));
				}
				Route route = builder.build();
				newRoutes.add(route);
				if (isCompleted) {
					completedRoutes.add(route);
				} else {
					pendingRoutes.add(route);
				}
			}
			List<Route> savedNewRoutes = routeRepository.saveAll(newRoutes);
			System.out.println("✅ Rutas nuevas creadas: " + savedNewRoutes.size());

// ➕ Asociar productos ya existentes
			List<Product> baseProducts = productRepository.findAll();
			List<Product> selectedProducts = baseProducts.subList(0, Math.min(3, baseProducts.size()));

// ➕ Crear 10 entregas, cada una con una de las nuevas rutas

			List<String> neighborhood = List.of(
							"Palermo",
							"Villa Crespo",
							"Villa Urquiza",
							"Villa Ortuzar",
							"Villa del Parque",
							"San Nicolás",
							"Puerto Madero",
							"La Boca",
							"Villa Crepos",
							"Villa Crespo",
							"Villa Crespo"
			);

			List<Delivery> newDeliveries = new ArrayList<>();
			for (int i = 0; i < savedNewRoutes.size(); i++) {
				Route route = savedNewRoutes.get(i);
				boolean isDelivered = route.getStatus() == RouteStatus.COMPLETED;

				Delivery.DeliveryBuilder deliveryBuilder = Delivery.builder()
								.destination(neighborhood.get(i))
								.packageLocation("Sector A - Estante " + (i))
								.createdDate(Timestamp.valueOf(LocalDateTime.now().minusDays(5 - i)))
								.deliveryStartDate(isDelivered ? Timestamp.valueOf(LocalDateTime.now().minusDays(4 - i)) : null)
								.deliveryEndDate(isDelivered ? Timestamp.valueOf(LocalDateTime.now().minusDays(3 - i)) : null)
								.status(isDelivered ? DeliveryStatus.DELIVERED : DeliveryStatus.NOT_DELIVERED)
								.pin(String.valueOf(1000 + i))
								.route(route)
								.products(selectedProducts);

				newDeliveries.add(deliveryBuilder.build());
			}
			List<Delivery> savedNewDeliveries = deliveryRepository.saveAll(newDeliveries);

// ➕ Generar QR codes para las nuevas entregas
			for (Delivery d : savedNewDeliveries) {
				String qr = QRCodeGenerator.generateQRCodeBase64(d.getId());
				d.setQrCode(qr);
			}
			deliveryRepository.saveAll(newDeliveries);

			System.out.println("✅ Entregas nuevas creadas y asociadas a rutas nuevas.");
		};
	}
}
