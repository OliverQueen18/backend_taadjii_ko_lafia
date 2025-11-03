package com.example.fuelticket.service;

import com.example.fuelticket.dto.UserDto;
import com.example.fuelticket.dto.AddUserToStationRequest;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.repository.UserRepository;
import com.example.fuelticket.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
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

        // Si c'est un utilisateur de station, associer à la station (collection)
        if (userDto.getStationId() != null) {
            Station station = stationRepository.findById(userDto.getStationId())
                    .orElseThrow(() -> new RuntimeException("Station non trouvée"));
            List<Station> stations = new ArrayList<>();
            stations.add(station);
            user.setStations(stations);
        }

        User savedUser = userRepository.save(user);
        
        // Envoyer le code de vérification
        verificationService.sendVerificationCode(savedUser);
        
        return convertToDto(savedUser);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Vérifier l'unicité du téléphone si fourni et différent de l'actuel
        if (userDto.getTelephone() != null && !userDto.getTelephone().trim().isEmpty()) {
            if (!userDto.getTelephone().equals(user.getTelephone())) {
                if (userRepository.existsByTelephone(userDto.getTelephone())) {
                    throw new RuntimeException("Un compte avec ce numéro de téléphone existe déjà");
                }
            }
        }

        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        user.setTelephone(userDto.getTelephone());
        user.setRole(userDto.getRole());

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto addUserToStation(Long stationId, AddUserToStationRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Vérifier que le téléphone est fourni et unique pour les citoyens
        if (request.getTelephone() == null || request.getTelephone().trim().isEmpty()) {
            throw new RuntimeException("Le numéro de téléphone est obligatoire");
        }
        if (userRepository.existsByTelephone(request.getTelephone())) {
            throw new RuntimeException("Un compte avec ce numéro de téléphone existe déjà");
        }

        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telephone(request.getTelephone())
                .role(User.Role.CITOYEN) // Par défaut, les utilisateurs ajoutés sont des citoyens
                .emailVerified(false)
                .build();
        // Associer la station à la collection de l'utilisateur
        user.setStations(new ArrayList<>(List.of(station)));

        User savedUser = userRepository.save(user);
        
        // Envoyer le code de vérification
        verificationService.sendVerificationCode(savedUser);
        
        return convertToDto(savedUser);
    }

    public List<UserDto> getUsersByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        return userRepository.findByStationsContains(station).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean verifyEmail(String email, String code) {
        return verificationService.verifyCode(email, code);
    }

    public void resendVerificationCode(String email) {
        verificationService.resendVerificationCode(email);
    }

    /**
     * Vérifie le code de vérification du téléphone
     */
    public boolean verifyTelephone(String telephone, String code) {
        return verificationService.verifyTelephoneCode(telephone, code);
    }
    
    /**
     * Renvoie le code de vérification du téléphone
     */
    public void resendTelephoneVerificationCode(String telephone) {
        verificationService.resendTelephoneVerificationCode(telephone);
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setTelephone(user.getTelephone());
        dto.setRole(user.getRole());
        dto.setEmailVerified(user.getEmailVerified());
        Long firstStationId = (user.getStations() != null && !user.getStations().isEmpty())
                ? user.getStations().get(0).getId() : null;
        dto.setStationId(firstStationId);
        return dto;
    }
}
