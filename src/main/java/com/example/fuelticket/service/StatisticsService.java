package com.example.fuelticket.service;

import com.example.fuelticket.dto.StatisticsDto;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.repository.UserRepository;
import com.example.fuelticket.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;

    public StatisticsDto getPublicStatistics() {
        long citizensCount = userRepository.countByRole(User.Role.CITOYEN);
        long stationsCount = stationRepository.count();
        long availableStationsCount = stationRepository.countStationsWithAvailableStock();
        
        return new StatisticsDto(citizensCount, stationsCount, availableStationsCount);
    }
}

