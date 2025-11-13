package com.example.fuelticket.service;

import com.example.fuelticket.dto.AuthRequest;
import com.example.fuelticket.dto.AuthResponse;
import com.example.fuelticket.dto.UserDto;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.entity.PendingRegistration;
import com.example.fuelticket.repository.UserRepository;
import com.example.fuelticket.repository.PendingRegistrationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import com.example.fuelticket.security.UserPrincipal;
import com.example.fuelticket.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    @Transactional
    public AuthResponse login(AuthRequest authRequest) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));
        
        // Vérifier si le compte est bloqué
        if (user.isAccountLocked()) {
            long minutesRemaining = user.getMinutesUntilUnlock();
            throw new RuntimeException(String.format(
                "Compte temporairement désactivé. Veuillez réessayer dans %d minute(s).", 
                minutesRemaining + 1
            ));
        }
        
        try {
            // Tenter l'authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            // Connexion réussie : réinitialiser les tentatives
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            user.setLastFailedLoginAttempt(null);
            userRepository.save(user);

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
                    
        } catch (BadCredentialsException e) {
            // Mot de passe incorrect : incrémenter les tentatives
            handleFailedLogin(user);
            
            // Recharger l'utilisateur pour avoir les dernières informations
            user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));
            
            int remainingAttempts = MAX_FAILED_ATTEMPTS - (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0);
            remainingAttempts = Math.max(0, remainingAttempts);
            
            String errorMessage = "Email ou mot de passe incorrect";
            if (remainingAttempts > 0) {
                errorMessage += String.format(". %d tentative(s) restante(s).", remainingAttempts);
            } else if (user.isAccountLocked()) {
                long minutesRemaining = user.getMinutesUntilUnlock();
                errorMessage = String.format(
                    "Compte temporairement désactivé après 3 tentatives échouées. Veuillez réessayer dans %d minute(s).", 
                    minutesRemaining + 1
                );
            }
            
            throw new RuntimeException(errorMessage);
        } catch (AuthenticationException e) {
            // Autre erreur d'authentification
            handleFailedLogin(user);
            throw new RuntimeException("Erreur d'authentification : " + e.getMessage());
        }
    }
    
    /**
     * Gère une tentative de connexion échouée
     */
    private void handleFailedLogin(User user) {
        int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLoginAttempt(LocalDateTime.now());
        
        System.out.println("🔴 handleFailedLogin - User: " + user.getEmail() + ", Attempts: " + attempts);
        
        // Si 3 tentatives ou plus, bloquer le compte pour 15 minutes
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            System.out.println("🔒 Account locked until: " + user.getAccountLockedUntil());
        }
        
        userRepository.save(user);
        System.out.println("💾 User saved with " + user.getFailedLoginAttempts() + " failed attempts");
    }

    @Transactional
    public AuthResponse register(UserDto userDto) {
        // Vérifier si un utilisateur existe déjà avec cet email
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }
        
        // Vérifier si une inscription en attente existe déjà avec cet email
        if (pendingRegistrationRepository.existsByEmail(userDto.getEmail())) {
            // Supprimer l'ancienne inscription en attente pour permettre une nouvelle inscription
            pendingRegistrationRepository.findByEmail(userDto.getEmail())
                .ifPresent(pendingRegistrationRepository::delete);
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
            // Vérifier l'unicité du téléphone (dans User et PendingRegistration)
            if (userRepository.existsByTelephone(userDto.getTelephone())) {
                throw new RuntimeException("Un compte avec ce numéro de téléphone existe déjà");
            }
            if (pendingRegistrationRepository.existsByTelephone(userDto.getTelephone())) {
                throw new RuntimeException("Une inscription en attente existe déjà avec ce numéro de téléphone");
            }
        } else if (userDto.getTelephone() != null && !userDto.getTelephone().trim().isEmpty()) {
            // Pour les autres rôles, vérifier l'unicité si un téléphone est fourni
            if (userRepository.existsByTelephone(userDto.getTelephone())) {
                throw new RuntimeException("Un compte avec ce numéro de téléphone existe déjà");
            }
            if (pendingRegistrationRepository.existsByTelephone(userDto.getTelephone())) {
                throw new RuntimeException("Une inscription en attente existe déjà avec ce numéro de téléphone");
            }
        }

        // Générer le code de vérification
        String verificationCode = verificationService.generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime codeExpiry = now.plusMinutes(15); // Code valide 15 minutes

        // Sérialiser les données utilisateur en JSON pour stockage temporaire
        String userDataJson;
        try {
            userDataJson = objectMapper.writeValueAsString(userDto);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sérialisation des données utilisateur", e);
        }

        // Créer l'inscription en attente (NE PAS créer l'utilisateur maintenant)
        PendingRegistration pendingRegistration = PendingRegistration.builder()
                .email(userDto.getEmail())
                .telephone(userDto.getTelephone())
                .verificationCode(verificationCode)
                .verificationCodeExpiry(codeExpiry)
                .userDataJson(userDataJson)
                .createdAt(now)
                .build();

        // Si c'est un citoyen, générer aussi un code SMS
        if (userDto.getRole() == User.Role.CITOYEN && userDto.getTelephone() != null) {
            String telephoneCode = verificationService.generateVerificationCode();
            pendingRegistration.setTelephoneVerificationCode(telephoneCode);
            pendingRegistration.setTelephoneVerificationCodeExpiry(codeExpiry);
        }

        PendingRegistration savedPending = pendingRegistrationRepository.save(pendingRegistration);

        // Envoyer le code de vérification par email (sans créer l'utilisateur)
        verificationService.sendVerificationEmailForPending(userDto.getEmail(), userDto.getNom(), verificationCode);
        
        // Si c'est un citoyen, envoyer aussi le code SMS
        if (userDto.getRole() == User.Role.CITOYEN && userDto.getTelephone() != null) {
            verificationService.sendVerificationSmsForPending(
                userDto.getTelephone(), 
                userDto.getNom(), 
                savedPending.getTelephoneVerificationCode()
            );
        }

        // Retourner une réponse sans token (l'utilisateur doit vérifier son email)
        return AuthResponse.builder()
                .token(null) // Pas de token jusqu'à la vérification
                .id(null) // Pas d'ID car l'utilisateur n'existe pas encore
                .nom(userDto.getNom())
                .prenom(userDto.getPrenom())
                .email(userDto.getEmail())
                .role(userDto.getRole())
                .emailVerified(false)
                .message("Un code de vérification a été envoyé à votre adresse email. Veuillez vérifier votre email pour finaliser votre inscription.")
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
    
    /**
     * Récupère les informations de sécurité pour un email (tentatives restantes, statut de blocage)
     */
    public Map<String, Object> getLoginSecurityInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte n'est associé à cet email"));
        
        Map<String, Object> securityInfo = new HashMap<>();
        int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        int remainingAttempts = Math.max(0, MAX_FAILED_ATTEMPTS - attempts);
        boolean isLocked = user.isAccountLocked();
        long minutesUntilUnlock = isLocked ? user.getMinutesUntilUnlock() : 0;
        
        securityInfo.put("remainingAttempts", remainingAttempts);
        securityInfo.put("failedAttempts", attempts);
        securityInfo.put("accountLocked", isLocked);
        securityInfo.put("minutesUntilUnlock", minutesUntilUnlock);
        securityInfo.put("maxAttempts", MAX_FAILED_ATTEMPTS);
        
        System.out.println("🔵 getLoginSecurityInfo - Email: " + email + ", Attempts: " + attempts + ", Remaining: " + remainingAttempts + ", Locked: " + isLocked);
        
        return securityInfo;
    }
}
