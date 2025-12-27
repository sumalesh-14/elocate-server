package com.elocate.elocate.service;

import com.elocate.elocate.model.DeviceModel;
import com.elocate.elocate.model.MetalRate;
import com.elocate.elocate.model.MetalType;
import com.elocate.elocate.repository.MetalRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core calculation service for reward points
 * Formula: Final Points = Σ (Metal Quantity × Metal Rate) × Condition Multiplier
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RewardCalculationService {
    
    private final MetalRateRepository metalRateRepository;
    
    /**
     * Get current active metal rates
     * @return Map of MetalType to MetalRate
     */
    public Map<MetalType, MetalRate> getCurrentMetalRates() {
        log.info("Fetching current active metal rates");
        LocalDate today = LocalDate.now();
        
        Map<MetalType, MetalRate> ratesMap = new HashMap<>();
        
        for (MetalType metalType : MetalType.values()) {
            metalRateRepository
                    .findFirstByMetalTypeAndIsActiveTrueAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(
                            metalType, today, today)
                    .ifPresent(rate -> ratesMap.put(metalType, rate));
        }
        
        log.info("Found {} active metal rates", ratesMap.size());
        return ratesMap;
    }
    
    /**
     * Calculate base metal points from device model
     * Formula: Σ (Metal Quantity × Metal Rate)
     * 
     * @param deviceModel Device model with metal composition
     * @param metalRates Map of current metal rates
     * @return Total metal points
     */
    public BigDecimal calculateMetalPoints(DeviceModel deviceModel, Map<MetalType, MetalRate> metalRates) {
        log.info("Calculating metal points for device model: {}", deviceModel.getModelName());
        
        BigDecimal totalPoints = BigDecimal.ZERO;
        
        // Calculate GOLD points (mg × rate)
        if (deviceModel.getGoldMg() != null && metalRates.containsKey(MetalType.GOLD)) {
            BigDecimal goldPoints = deviceModel.getGoldMg()
                    .multiply(metalRates.get(MetalType.GOLD).getPointsPerUnit());
            totalPoints = totalPoints.add(goldPoints);
            log.debug("Gold: {} mg × {} pts/mg = {} pts", 
                    deviceModel.getGoldMg(), 
                    metalRates.get(MetalType.GOLD).getPointsPerUnit(), 
                    goldPoints);
        }
        
        // Calculate SILVER points (mg × rate)
        if (deviceModel.getSilverMg() != null && metalRates.containsKey(MetalType.SILVER)) {
            BigDecimal silverPoints = deviceModel.getSilverMg()
                    .multiply(metalRates.get(MetalType.SILVER).getPointsPerUnit());
            totalPoints = totalPoints.add(silverPoints);
            log.debug("Silver: {} mg × {} pts/mg = {} pts", 
                    deviceModel.getSilverMg(), 
                    metalRates.get(MetalType.SILVER).getPointsPerUnit(), 
                    silverPoints);
        }
        
        // Calculate COPPER points (g × rate)
        if (deviceModel.getCopperG() != null && metalRates.containsKey(MetalType.COPPER)) {
            BigDecimal copperPoints = deviceModel.getCopperG()
                    .multiply(metalRates.get(MetalType.COPPER).getPointsPerUnit());
            totalPoints = totalPoints.add(copperPoints);
            log.debug("Copper: {} g × {} pts/g = {} pts", 
                    deviceModel.getCopperG(), 
                    metalRates.get(MetalType.COPPER).getPointsPerUnit(), 
                    copperPoints);
        }
        
        // Calculate PALLADIUM points (mg × rate)
        if (deviceModel.getPalladiumMg() != null && metalRates.containsKey(MetalType.PALLADIUM)) {
            BigDecimal palladiumPoints = deviceModel.getPalladiumMg()
                    .multiply(metalRates.get(MetalType.PALLADIUM).getPointsPerUnit());
            totalPoints = totalPoints.add(palladiumPoints);
            log.debug("Palladium: {} mg × {} pts/mg = {} pts", 
                    deviceModel.getPalladiumMg(), 
                    metalRates.get(MetalType.PALLADIUM).getPointsPerUnit(), 
                    palladiumPoints);
        }
        
        log.info("Total metal points: {}", totalPoints);
        return totalPoints.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Apply condition multiplier to base points
     * Formula: Base Points × Condition Multiplier
     * 
     * @param basePoints Base metal points
     * @param multiplier Condition multiplier
     * @return Final points after multiplier
     */
    public BigDecimal applyConditionMultiplier(BigDecimal basePoints, BigDecimal multiplier) {
        log.info("Applying condition multiplier: {} × {}", basePoints, multiplier);
        
        BigDecimal finalPoints = basePoints.multiply(multiplier);
        log.info("Final points after multiplier: {}", finalPoints);
        
        return finalPoints.setScale(2, RoundingMode.HALF_UP);
    }
}
