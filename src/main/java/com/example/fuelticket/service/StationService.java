package com.example.fuelticket.service;

import com.example.fuelticket.dto.StationDto;
import com.example.fuelticket.dto.StationWithStocksDto;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.entity.Region;
import com.example.fuelticket.repository.StationRepository;
import com.example.fuelticket.repository.TicketRepository;
import com.example.fuelticket.repository.SaleScheduleRepository;
import com.example.fuelticket.repository.UserRepository;
import com.example.fuelticket.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final AuthService authService;
    private final TicketRepository ticketRepository;
    private final SaleScheduleRepository saleScheduleRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    @Cacheable(value = "stations", key = "'all'")
    public List<StationDto> getAllStations() {
        // Filtrer par manager si l'utilisateur est un gérant de station
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == User.Role.STATION) {
                // Retourner uniquement les stations du manager avec région chargée
                return stationRepository.findByManagerIdWithRegion(currentUser.getId()).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Si l'utilisateur n'est pas authentifié ou erreur, continuer avec toutes les stations
        }
        
        // Pour les admins ou utilisateurs non authentifiés, retourner toutes les stations avec région chargée
        return stationRepository.findAllWithRegion().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Note: getMyStations() n'est pas mis en cache car il dépend de l'utilisateur actuel
    // Le cache serait trop complexe à gérer avec des clés dynamiques basées sur l'utilisateur
    public List<StationDto> getMyStations() {
        User currentUser = authService.getCurrentUser();
        return stationRepository.findByManagerIdWithRegion(currentUser.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "stations", key = "'all-with-stocks'")
    public List<StationWithStocksDto> getAllStationsWithStocks() {
        // Filtrer par manager si l'utilisateur est un gérant de station
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == User.Role.STATION) {
                // Retourner uniquement les stations du manager avec région et stocks chargés
                return stationRepository.findByManagerIdWithRegionAndStocks(currentUser.getId()).stream()
                        .map(StationWithStocksDto::fromStationWithStocks)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Si l'utilisateur n'est pas authentifié ou erreur, continuer avec toutes les stations
            // C'est utile pour les endpoints publics ou non authentifiés
        }
        
        // Pour les admins ou utilisateurs non authentifiés, retourner toutes les stations avec région et stocks chargés
        return stationRepository.findAllWithRegionAndStocks().stream()
                .map(StationWithStocksDto::fromStationWithStocks)
                .collect(Collectors.toList());
    }
    
    // Note: getMyStationsWithStocks() n'est pas mis en cache car il dépend de l'utilisateur actuel
    public List<StationWithStocksDto> getMyStationsWithStocks() {
        User currentUser = authService.getCurrentUser();
        return stationRepository.findByManagerIdWithRegionAndStocks(currentUser.getId()).stream()
                .map(StationWithStocksDto::fromStationWithStocks)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "stations", key = "#id")
    public StationDto getStationById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));
        return convertToDto(station);
    }

    public StationWithStocksDto getStationWithStocksById(Long id) {
        Station station = stationRepository.findByIdWithRegionAndStocks(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));
        return StationWithStocksDto.fromStationWithStocks(station);
    }

    @Transactional
    @CacheEvict(value = "stations", allEntries = true)
    public StationDto createStation(StationDto stationDto) {
        User currentUser = authService.getCurrentUser();
        
        // Récupérer la région
        Region region = null;
        if (stationDto.getRegionId() != null) {
            region = regionRepository.findById(stationDto.getRegionId())
                    .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + stationDto.getRegionId()));
        } else {
            throw new RuntimeException("La région est obligatoire pour créer une station");
        }
        
        Station.StationBuilder builder = Station.builder()
                .nom(stationDto.getNom())
                .localisation(stationDto.getLocalisation())
                .capaciteJournaliere(stationDto.getCapaciteJournaliere())
                .adresseComplete(stationDto.getAdresseComplete())
                .latitude(stationDto.getLatitude())
                .longitude(stationDto.getLongitude())
                .telephone(stationDto.getTelephone())
                .email(stationDto.getEmail())
                .siteWeb(stationDto.getSiteWeb())
                .horairesOuverture(stationDto.getHorairesOuverture())
                .isOuverte(stationDto.getIsOuverte() != null ? stationDto.getIsOuverte() : true)
                .region(region);
        
        // Lier le manager si l'utilisateur est STATION ou ADMIN (pour ADMIN, on peut permettre de ne pas lier)
        if (currentUser.getRole() == User.Role.STATION) {
            builder.manager(currentUser);
        }
        
        Station station = builder.build();
        Station savedStation = stationRepository.save(station);
        return convertToDto(savedStation);
    }

    @Transactional
    @CacheEvict(value = "stations", allEntries = true)
    public StationDto updateStation(Long id, StationDto stationDto) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));

        // Vérification de sécurité : un gérant de station ne peut modifier que ses propres stations
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == User.Role.STATION) {
            // Un gérant de station ne peut modifier que ses propres stations
            if (station.getManager() == null || !station.getManager().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Vous n'avez pas l'autorisation de modifier cette station");
            }
        }

        // Mettre à jour tous les champs
        station.setNom(stationDto.getNom());
        station.setLocalisation(stationDto.getLocalisation());
        station.setCapaciteJournaliere(stationDto.getCapaciteJournaliere());
        station.setAdresseComplete(stationDto.getAdresseComplete());
        station.setLatitude(stationDto.getLatitude());
        station.setLongitude(stationDto.getLongitude());
        station.setTelephone(stationDto.getTelephone());
        station.setEmail(stationDto.getEmail());
        station.setSiteWeb(stationDto.getSiteWeb());
        station.setHorairesOuverture(stationDto.getHorairesOuverture());
        
        // Mettre à jour la région si fournie
        if (stationDto.getRegionId() != null) {
            Region region = regionRepository.findById(stationDto.getRegionId())
                    .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + stationDto.getRegionId()));
            station.setRegion(region);
        }
        
        // Mettre à jour isOuverte si fourni
        if (stationDto.getIsOuverte() != null) {
            station.setIsOuverte(stationDto.getIsOuverte());
        }
        
        // Le manager_id ne peut être modifié que par un ADMIN
        // Si un ADMIN envoie un managerId différent, on peut le mettre à jour
        if (currentUser != null && currentUser.getRole() == User.Role.ADMIN && stationDto.getManagerId() != null) {
            // Si un admin essaie de changer le manager, on pourrait le faire ici
            // Pour l'instant, on ne change pas le manager lors de la mise à jour
            // car cela nécessiterait de récupérer le User depuis le repository
        }
        // Sinon, on conserve le manager existant

        Station savedStation = stationRepository.save(station);
        return convertToDto(savedStation);
    }

    @Transactional
    @CacheEvict(value = "stations", allEntries = true)
    public void deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station non trouvée avec l'id: " + id));

        // Vérification de sécurité : un gérant de station ne peut supprimer que ses propres stations
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == User.Role.STATION) {
            // Un gérant de station ne peut supprimer que ses propres stations
            if (station.getManager() == null || !station.getManager().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Vous n'avez pas l'autorisation de supprimer cette station");
            }
        }

        // Vérifier s'il y a des tickets associés à cette station
        long ticketCount = ticketRepository.findByStation(station).size();
        if (ticketCount > 0) {
            throw new RuntimeException(
                String.format("Impossible de supprimer la station. Elle est associée à %d ticket(s). " +
                    "Veuillez d'abord gérer ces tickets avant de supprimer la station.", ticketCount)
            );
        }

        // Supprimer les plannings de vente associés (SaleSchedule)
        // Les tickets liés aux SaleSchedule sont déjà vérifiés ci-dessus
        saleScheduleRepository.findByStation(station).forEach(saleScheduleRepository::delete);

        // Retirer cette station de la liste des stations des utilisateurs associés
        List<User> usersWithStation = userRepository.findByStationsContains(station);
        for (User user : usersWithStation) {
            if (user.getStations() != null) {
                user.getStations().removeIf(s -> s.getId().equals(id));
                userRepository.save(user);
            }
        }

        // Les FuelStock seront supprimés automatiquement grâce au cascade = CascadeType.ALL
        // dans l'entité Station

        // Supprimer la station
        stationRepository.deleteById(id);
    }

    private StationDto convertToDto(Station station) {
        StationDto dto = new StationDto();
        dto.setId(station.getId());
        dto.setNom(station.getNom());
        dto.setLocalisation(station.getLocalisation());
        dto.setCapaciteJournaliere(station.getCapaciteJournaliere());
        // stockDisponible n'existe pas dans StationDto, il est calculé via StationWithStocksDto
        dto.setAdresseComplete(station.getAdresseComplete());
        dto.setLatitude(station.getLatitude());
        dto.setLongitude(station.getLongitude());
        dto.setTelephone(station.getTelephone());
        dto.setEmail(station.getEmail());
        dto.setSiteWeb(station.getSiteWeb());
        dto.setHorairesOuverture(station.getHorairesOuverture());
        dto.setIsOuverte(station.getIsOuverte());
        dto.setManagerId(station.getManager() != null ? station.getManager().getId() : null);
        
        // Informations de la région
        if (station.getRegion() != null) {
            dto.setRegionId(station.getRegion().getId());
            dto.setRegionNom(station.getRegion().getNom());
            dto.setRegionCode(station.getRegion().getCode());
        }
        
        return dto;
    }
}
