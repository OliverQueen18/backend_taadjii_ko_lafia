package com.example.fuelticket.config;

import com.example.fuelticket.entity.User;
import com.example.fuelticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "services@taadjiikolafia.org";
    private static final String ADMIN_NOM = "Administrateur";
    private static final String ADMIN_PRENOM = "Global";
    private static final String ADMIN_PASSWORD = "Services@2025*!";

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si l'utilisateur admin existe déjà
        if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
            log.info("Création de l'utilisateur administrateur par défaut...");
            
            User adminUser = User.builder()
                    .nom(ADMIN_NOM)
                    .prenom(ADMIN_PRENOM)
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(User.Role.ADMIN)
                    .emailVerified(true) // L'admin est automatiquement vérifié
                    .telephoneVerified(true) // L'admin est automatiquement vérifié
                    .build();

            userRepository.save(adminUser);
            log.info("Utilisateur administrateur créé avec succès: {}", ADMIN_EMAIL);
        } else {
            log.info("L'utilisateur administrateur existe déjà: {}", ADMIN_EMAIL);
        }
    }
}

