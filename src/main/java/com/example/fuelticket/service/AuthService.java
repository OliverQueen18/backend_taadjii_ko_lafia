package com.example.fuelticket.service;

import com.example.fuelticket.dto.AuthRequest;
import com.example.fuelticket.dto.AuthResponse;
import com.example.fuelticket.dto.UserDto;
import com.example.fuelticket.dto.StationDto;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.Region;
import com.example.fuelticket.repository.UserRepository;
import com.example.fuelticket.repository.StationRepository;
import com.example.fuelticket.repository.RegionRepository;
import com.example.fuelticket.security.UserPrincipal;
import com.example.fuelticket.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;
    private final StationRepository stationRepository;
    private final RegionRepository regionRepository;

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userPrincipal);

        return AuthResponse.builder()
                .token(token)
                .id(userPrincipal.getId())
                .nom(userPrincipal.getNom())
                .prenom(userPrincipal.getPrenom())
                .email(userPrincipal.getEmail())
                .role(userPrincipal.getRole())
                .build();
    }

    @Transactional
    public AuthResponse register(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        // Vérifier que le mot de passe est requis pour l'inscription
        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Le mot de passe est obligatoire");
        }
        if (userDto.getPassword().length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }

        // Vérifier que le téléphone est requis pour les citoyens
        if (userDto.getRole() == User.Role.CITOYEN) {
            if (userDto.getTelephone() == null || userDto.getTelephone().trim().isEmpty()) {
                throw new RuntimeException("Le numéro de téléphone est obligatoire pour les citoyens");
            }
            // Vérifier l'unicité du téléphone
            if (userRepository.existsByTelephone(userDto.getTelephone())) {
                throw new RuntimeException("Un compte avec ce numéro de téléphone existe déjà");
            }
        } else if (userDto.getTelephone() != null && !userDto.getTelephone().trim().isEmpty()) {
            // Pour les autres rôles, vérifier l'unicité si un téléphone est fourni
            if (userRepository.existsByTelephone(userDto.getTelephone())) {
                throw new RuntimeException("Un compte avec ce numéro de téléphone existe déjà");
            }
        }

        User user = User.builder()
                .nom(userDto.getNom())
                .prenom(userDto.getPrenom())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .telephone(userDto.getTelephone())
                .role(userDto.getRole())
                .emailVerified(false) // Email non vérifié par défaut
                .build();

        User savedUser = userRepository.save(user);
        
        // Si l'utilisateur est un gérant de station et qu'une station est fournie, la créer automatiquement
        if (userDto.getRole() == User.Role.STATION && userDto.getStation() != null) {
            StationDto sd = userDto.getStation();
            
            // Log pour déboguer
            System.out.println("DEBUG AuthService: StationDto reçu - regionId: " + sd.getRegionId());
            
            // Récupérer la région
            Region region = null;
            if (sd.getRegionId() != null) {
                System.out.println("DEBUG AuthService: Recherche de la région avec l'id: " + sd.getRegionId());
                region = regionRepository.findById(sd.getRegionId())
                        .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + sd.getRegionId()));
                System.out.println("DEBUG AuthService: Région trouvée - id: " + region.getId() + ", nom: " + region.getNom());
            } else {
                System.out.println("DEBUG AuthService: ERREUR - regionId est null dans StationDto");
                throw new RuntimeException("La région est obligatoire pour créer une station");
            }
            
            Station.StationBuilder stationBuilder = Station.builder()
                    .nom(sd.getNom())
                    .localisation(sd.getLocalisation())
                    .capaciteJournaliere(sd.getCapaciteJournaliere())
                    .adresseComplete(sd.getAdresseComplete())
                    .latitude(sd.getLatitude())
                    .longitude(sd.getLongitude())
                    .telephone(sd.getTelephone())
                    .email(sd.getEmail())
                    .siteWeb(sd.getSiteWeb())
                    .horairesOuverture(sd.getHorairesOuverture())
                    .isOuverte(sd.getIsOuverte() != null ? sd.getIsOuverte() : true)
                    .manager(savedUser)
                    .region(region);
            
            Station station = stationBuilder.build();
            System.out.println("DEBUG AuthService: Station créée - region: " + (station.getRegion() != null ? station.getRegion().getId() : "NULL"));
            stationRepository.save(station);
            System.out.println("DEBUG AuthService: Station sauvegardée avec succès");
        }
        
        // Envoyer le code de vérification par email
        verificationService.sendVerificationCode(savedUser);

        // Retourner une réponse sans token (l'utilisateur doit vérifier son email)
        return AuthResponse.builder()
                .token(null) // Pas de token jusqu'à la vérification
                .id(savedUser.getId())
                .nom(savedUser.getNom())
                .prenom(savedUser.getPrenom())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .emailVerified(false)
                .message("Un code de vérification a été envoyé à votre adresse email")
                .build();
    }

    public AuthResponse verifyEmailAndLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getEmailVerified()) {
            throw new RuntimeException("Email not verified");
        }
        
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String token = jwtUtil.generateToken(userPrincipal);
        
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(true)
                .message("Email vérifié avec succès !")
                .build();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("User not authenticated");
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte n'est associé à cet email"));
        
        // Envoyer un code de réinitialisation de mot de passe
        verificationService.sendPasswordResetCode(user);
    }
}
