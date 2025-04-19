package com.matiasugluck.deremate_backend;
import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken; // Necesario si VerificationService lo usa internamente y quieres ser más específico
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.repository.VerificationTokenRepository; // Mock necesario para VerificationService
import com.matiasugluck.deremate_backend.service.impl.AuthServiceImpl;
import com.matiasugluck.deremate_backend.service.impl.EmailService;
import com.matiasugluck.deremate_backend.service.impl.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; // Importa todo Mockito
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito para JUnit 5
class AuthServiceTest {

    @InjectMocks // Crea un mock para esta dependencia
    private UserRepository userRepository;

    @InjectMocks // Crea un mock
    private PasswordEncoder passwordEncoder;

    // --- Mocks para las dependencias de VerificationService ---
    // Necesitamos mockear EmailService porque VerificationService lo usa
    @InjectMocks
    private EmailService emailService;
    // Necesitamos mockear VerificationTokenRepository porque VerificationService lo usa
    @InjectMocks
    private VerificationTokenRepository verificationTokenRepository;
    // --------------------------------------------------------

    // Creamos una instancia REAL de VerificationService pero le inyectamos los MOCKS anteriores
    // Esto nos permite verificar la interacción con VerificationService sin mockearlo directamente,
    // y así también probamos indirectamente que VerificationService llama a EmailService.
    @Spy // O @InjectMocks si VerificationService no tiene lógica compleja que quieras mantener
    @InjectMocks // Asegúrate de que VerificationService tenga @RequiredArgsConstructor o un constructor adecuado
    private VerificationService verificationService;


    // Inyecta TODOS los mocks anteriores en la instancia de AuthServiceImpl
    @InjectMocks
    private AuthServiceImpl authService; // La clase que estamos probando

    // Captor para verificar el objeto User guardado
    @Captor
    private ArgumentCaptor<User> userCaptor;

    // Captor para verificar el token de verificación (opcional, pero útil)
    @Captor
    private ArgumentCaptor<VerificationToken> tokenCaptor;

    // Captor para verificar el email y el código enviados
    @Captor
    private ArgumentCaptor<String> emailToCaptor;
    @Captor
    private ArgumentCaptor<String> codeCaptor;


    // Datos de prueba comunes
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        // Inicializa datos comunes antes de cada test
        email = "test@example.com";
        password = "password123";
        firstName = "Test";
        lastName = "User";
        encodedPassword = "encodedPassword123";

        // Configuración común de mocks (puede sobreescribirse en tests específicos)
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        // Importante: Simula que el save devuelve el objeto guardado (comportamiento común de JPA)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void signup_WhenUserDoesNotExist_ShouldSaveUserAndTriggerVerification() throws Exception { // Añade throws si sendEmail lo hace
        // Arrange
        // Simula que el usuario NO existe
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        // Simula que no hay token existente para este usuario (necesario para resendVerification)
        when(verificationTokenRepository.findByUser(any(User.class))).thenReturn(null);

        // Act
        GenericResponseDTO<Void> response = authService.signup(email, password, firstName, lastName);

        // Assert
        // 1. Verifica la respuesta del servicio
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getMessage()).contains(email); // Verifica que el mensaje incluya el email

        // 2. Verifica que se guardó el usuario correcto
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword); // Contraseña codificada
        assertThat(savedUser.isEmailVerified()).isFalse(); // Usuario inicia no verificado

        // 3. Verifica que se creó y guardó un token de verificación
        //    (Indirectamente a través de la llamada a verificationService.resendVerification)
        verify(verificationTokenRepository).save(tokenCaptor.capture());
        VerificationToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(savedUser); // Asociado al usuario correcto
        assertThat(savedToken.getToken()).isNotNull().hasSize(4); // Token generado

        // 4. Verifica que se llamó a EmailService para enviar el correo
        //    (Indirectamente a través de la llamada a verificationService.resendVerification)
        verify(emailService).sendVerificationEmail(emailToCaptor.capture(), codeCaptor.capture());
        assertThat(emailToCaptor.getValue()).isEqualTo(email); // Email correcto
        assertThat(codeCaptor.getValue()).isEqualTo(savedToken.getToken()); // Código correcto (el mismo que se guardó)

        // Verifica que findByEmail fue llamado una vez
        verify(userRepository, times(1)).findByEmail(email);
        // Verifica que la codificación se hizo una vez
        verify(passwordEncoder, times(1)).encode(password);
        // Verifica que verificationTokenRepository.findByUser fue llamado (dentro de resendVerification)
        verify(verificationTokenRepository, times(1)).findByUser(savedUser);
        // Verifica que emailService.sendVerificationEmail fue llamado una vez
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void signup_WhenUserAlreadyExists_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        // Simula que el usuario YA existe
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User())); // Devuelve un usuario existente


        // Verifica que no se intentó guardar nada ni enviar email si el usuario existe
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(verificationService, never()).resendVerification(anyString());
    }


}
