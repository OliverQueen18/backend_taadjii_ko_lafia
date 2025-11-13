package com.example.fuelticket.service;

import com.example.fuelticket.entity.User;
import com.example.fuelticket.entity.PendingRegistration;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.Region;
import com.example.fuelticket.repository.UserRepository;
import com.example.fuelticket.repository.PendingRegistrationRepository;
import com.example.fuelticket.repository.StationRepository;
import com.example.fuelticket.repository.RegionRepository;
import com.example.fuelticket.dto.UserDto;
import com.example.fuelticket.dto.StationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final StationRepository stationRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final String CODE_CHARACTERS = "0123456789";
    
    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        }
        
        return code.toString();
    }
    
    public void sendVerificationCode(User user) {
        String verificationCode = generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
        user.setVerificationCodeSentAt(now);
        
        userRepository.save(user);
        
        // Envoyer l'email
        emailService.sendVerificationEmail(user.getEmail(), user.getNom(), verificationCode);
        
        // Si c'est un citoyen, envoyer aussi un SMS de vérification
        if (user.getRole() == User.Role.CITOYEN && user.getTelephone() != null && !user.getTelephone().trim().isEmpty()) {
            sendTelephoneVerificationCode(user);
        }
    }
    
    /**
     * Envoie un code de vérification par SMS pour le téléphone
     */
    public void sendTelephoneVerificationCode(User user) {
        String verificationCode = generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        
        user.setTelephoneVerificationCode(verificationCode);
        user.setTelephoneVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
        user.setTelephoneVerificationCodeSentAt(now);
        
        userRepository.save(user);
        
        // Envoyer le SMS
        smsService.sendVerificationSms(user.getTelephone(), user.getNom(), verificationCode);
    }
    
    /**
     * Vérifie le code de vérification email et crée l'utilisateur si le code est valide
     * L'utilisateur n'est créé dans la table User qu'après vérification réussie
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        // Chercher d'abord dans les inscriptions en attente
        Optional<PendingRegistration> pendingOpt = pendingRegistrationRepository.findByEmail(email);
        
        if (pendingOpt.isPresent()) {
            // Inscription en attente trouvée - vérifier le code
            PendingRegistration pending = pendingOpt.get();
            
            // Vérifier si le code correspond et n'est pas expiré
            if (pending.getVerificationCode() == null || 
                !pending.getVerificationCode().equals(code) ||
                pending.isEmailCodeExpired()) {
                return false;
            }
            
            // Créer l'utilisateur maintenant que le code est vérifié
            // Le téléphoneVerified sera lu depuis pending.telephoneVerified
            User user = createUserFromPendingRegistration(pending);
            
            // Marquer l'inscription comme vérifiée et la supprimer
            pending.setVerifiedAt(LocalDateTime.now());
            pendingRegistrationRepository.delete(pending);
            
            // Envoyer l'email de bienvenue
            emailService.sendWelcomeEmail(user.getEmail(), user.getNom(), user.getRole().toString());
            
            return true;
        }
        
        // Fallback : chercher dans les utilisateurs existants (pour compatibilité avec les anciens comptes)
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Vérifier si le code correspond et n'est pas expiré
        if (user.getVerificationCode() == null || 
            !user.getVerificationCode().equals(code) ||
            user.getVerificationCodeExpiry() == null ||
            LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            return false;
        }
        
        // Marquer l'email comme vérifié
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationCodeSentAt(null);
        
        userRepository.save(user);
        
        // Envoyer l'email de bienvenue
        emailService.sendWelcomeEmail(user.getEmail(), user.getNom(), user.getRole().toString());
        
        return true;
    }
    
    /**
     * Crée un utilisateur à partir d'une inscription en attente vérifiée
     */
    @Transactional
    private User createUserFromPendingRegistration(PendingRegistration pending) {
        try {
            // Désérialiser les données utilisateur
            UserDto userDto = objectMapper.readValue(pending.getUserDataJson(), UserDto.class);
            
            // Vérifier à nouveau que l'email n'existe pas (au cas où)
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                throw new RuntimeException("Un compte avec cet email existe déjà");
            }
            
            // Créer l'utilisateur
            User user = User.builder()
                    .nom(userDto.getNom())
                    .prenom(userDto.getPrenom())
                    .email(userDto.getEmail())
                    .password(passwordEncoder.encode(userDto.getPassword()))
                    .telephone(userDto.getTelephone())
                    .role(userDto.getRole())
                    .emailVerified(true) // Email vérifié car le code a été validé
                    .telephoneVerified(pending.getTelephoneVerified() != null ? pending.getTelephoneVerified() : false) // Téléphone vérifié si le code SMS a été validé
                    .build();
            
            User savedUser = userRepository.save(user);
            
            // Si c'est un gérant de station et qu'une station est fournie, la créer
            if (userDto.getRole() == User.Role.STATION && userDto.getStation() != null) {
                StationDto sd = userDto.getStation();
                
                // Récupérer la région
                Region region = null;
                if (sd.getRegionId() != null) {
                    region = regionRepository.findById(sd.getRegionId())
                            .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + sd.getRegionId()));
                } else {
                    throw new RuntimeException("La région est obligatoire pour créer une station");
                }
                
                Station station = Station.builder()
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
                        .region(region)
                        .build();
                
                stationRepository.save(station);
            }
            
            return savedUser;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de l'utilisateur depuis l'inscription en attente", e);
        }
    }
    
    /**
     * Envoie un email de vérification pour une inscription en attente (sans utilisateur)
     */
    public void sendVerificationEmailForPending(String email, String nom, String code) {
        emailService.sendVerificationEmail(email, nom, code);
    }
    
    /**
     * Envoie un SMS de vérification pour une inscription en attente (sans utilisateur)
     */
    public void sendVerificationSmsForPending(String telephone, String nom, String code) {
        smsService.sendVerificationSms(telephone, nom, code);
    }
    
    public boolean isCodeExpired(String email) {
        // Chercher d'abord dans les inscriptions en attente
        Optional<PendingRegistration> pendingOpt = pendingRegistrationRepository.findByEmail(email);
        
        if (pendingOpt.isPresent()) {
            return pendingOpt.get().isEmailCodeExpired();
        }
        
        // Fallback : chercher dans les utilisateurs existants
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return true;
        }
        
        User user = userOpt.get();
        
        return user.getVerificationCodeExpiry() == null || 
               LocalDateTime.now().isAfter(user.getVerificationCodeExpiry());
    }
    
    public void resendVerificationCode(String email) {
        // Chercher d'abord dans les inscriptions en attente
        Optional<PendingRegistration> pendingOpt = pendingRegistrationRepository.findByEmail(email);
        
        if (pendingOpt.isPresent()) {
            PendingRegistration pending = pendingOpt.get();
            
            // Générer un nouveau code
            String newCode = generateVerificationCode();
            LocalDateTime now = LocalDateTime.now();
            
            pending.setVerificationCode(newCode);
            pending.setVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
            
            pendingRegistrationRepository.save(pending);
            
            // Envoyer le nouveau code par email
            // Extraire le nom depuis les données JSON
            try {
                UserDto userDto = objectMapper.readValue(pending.getUserDataJson(), UserDto.class);
                sendVerificationEmailForPending(email, userDto.getNom(), newCode);
            } catch (Exception e) {
                sendVerificationEmailForPending(email, "Utilisateur", newCode);
            }
            
            return;
        }
        
        // Fallback : chercher dans les utilisateurs existants
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Aucune inscription en attente ou utilisateur trouvé avec cet email");
        }
        
        User user = userOpt.get();
        
        if (user.getEmailVerified()) {
            throw new RuntimeException("L'email est déjà vérifié");
        }
        
        sendVerificationCode(user);
    }
    
    public void sendPasswordResetCode(User user) {
        String resetCode = generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        
        user.setVerificationCode(resetCode);
        user.setVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
        user.setVerificationCodeSentAt(now);
        
        userRepository.save(user);
        
        // Envoyer l'email de réinitialisation
        emailService.sendPasswordResetEmail(user.getEmail(), user.getNom(), resetCode);
    }
    
    /**
     * Vérifie le code de vérification du téléphone
     * Si l'utilisateur n'existe pas encore (inscription en attente), vérifie seulement le code
     * L'utilisateur sera créé lors de la vérification de l'email
     */
    @Transactional
    public boolean verifyTelephoneCode(String telephone, String code) {
        // Chercher d'abord dans les inscriptions en attente
        Optional<PendingRegistration> pendingOpt = pendingRegistrationRepository.findByTelephone(telephone);
        
        if (pendingOpt.isPresent()) {
            // Inscription en attente trouvée - vérifier le code SMS
            PendingRegistration pending = pendingOpt.get();
            
            // Vérifier si le code correspond et n'est pas expiré
            if (pending.getTelephoneVerificationCode() == null || 
                !pending.getTelephoneVerificationCode().equals(code) ||
                pending.isTelephoneCodeExpired()) {
                return false;
            }
            
            // Marquer le téléphone comme vérifié dans l'inscription en attente
            // L'utilisateur sera créé lors de la vérification de l'email avec telephoneVerified = true
            pending.setTelephoneVerified(true);
            pendingRegistrationRepository.save(pending);
            
            return true;
        }
        
        // Fallback : chercher dans les utilisateurs existants (pour compatibilité)
        Optional<User> userOpt = userRepository.findByTelephone(telephone);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Vérifier si le code correspond et n'est pas expiré
        if (user.getTelephoneVerificationCode() == null || 
            !user.getTelephoneVerificationCode().equals(code) ||
            user.getTelephoneVerificationCodeExpiry() == null ||
            LocalDateTime.now().isAfter(user.getTelephoneVerificationCodeExpiry())) {
            return false;
        }
        
        // Marquer le téléphone comme vérifié
        user.setTelephoneVerified(true);
        user.setTelephoneVerificationCode(null);
        user.setTelephoneVerificationCodeExpiry(null);
        user.setTelephoneVerificationCodeSentAt(null);
        
        userRepository.save(user);
        
        return true;
    }
    
    /**
     * Renvoie le code de vérification du téléphone
     */
    public void resendTelephoneVerificationCode(String telephone) {
        // Chercher d'abord dans les inscriptions en attente
        Optional<PendingRegistration> pendingOpt = pendingRegistrationRepository.findByTelephone(telephone);
        
        if (pendingOpt.isPresent()) {
            PendingRegistration pending = pendingOpt.get();
            
            // Générer un nouveau code SMS
            String newCode = generateVerificationCode();
            LocalDateTime now = LocalDateTime.now();
            
            pending.setTelephoneVerificationCode(newCode);
            pending.setTelephoneVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
            
            pendingRegistrationRepository.save(pending);
            
            // Envoyer le nouveau code par SMS
            // Extraire le nom depuis les données JSON
            try {
                UserDto userDto = objectMapper.readValue(pending.getUserDataJson(), UserDto.class);
                sendVerificationSmsForPending(telephone, userDto.getNom(), newCode);
            } catch (Exception e) {
                sendVerificationSmsForPending(telephone, "Utilisateur", newCode);
            }
            
            return;
        }
        
        // Fallback : chercher dans les utilisateurs existants
        Optional<User> userOpt = userRepository.findByTelephone(telephone);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Aucune inscription en attente ou utilisateur trouvé avec ce numéro de téléphone");
        }
        
        User user = userOpt.get();
        
        if (user.getTelephoneVerified()) {
            throw new RuntimeException("Le téléphone est déjà vérifié");
        }
        
        sendTelephoneVerificationCode(user);
    }
}
