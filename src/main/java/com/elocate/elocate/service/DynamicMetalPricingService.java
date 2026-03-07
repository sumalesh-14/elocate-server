package com.elocate.elocate.service;

import com.elocate.elocate.model.MetalRate;
import com.elocate.elocate.model.enums.MetalType;
import com.elocate.elocate.repository.MetalRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMetalPricingService {

    private final MetalRateRepository metalRateRepository;

    /**
     * Executes every hour to simulate fetching real metal market rates
     * automatically.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateMetalRatesFromExternalApi() {
        log.info("Fetching real-time metal rates from external market API (Simulated)...");

        // Here we simulate the external API response which slightly adjusts the prices
        List<MetalType> activeTypes = List.of(MetalType.GOLD, MetalType.SILVER, MetalType.COPPER, MetalType.PALLADIUM);

        for (MetalType type : activeTypes) {
            List<MetalRate> currentRates = metalRateRepository.findByMetalTypeAndIsActive(type, true);
            if (!currentRates.isEmpty()) {
                MetalRate currentRate = currentRates.get(0);

                // Simulate market fluctuation -5% to +5%
                double fluctuation = 1.0 + (Math.random() * 0.10 - 0.05);
                BigDecimal newPoints = currentRate.getPointsPerUnit()
                        .multiply(BigDecimal.valueOf(fluctuation))
                        .setScale(2, RoundingMode.HALF_UP);

                // Update rate directly to keep historical records clean (or create new rate
                // row)
                // For simplicity, updating the current active row.
                currentRate.setPointsPerUnit(newPoints);
                metalRateRepository.save(currentRate);

                log.info("Market updated rate for {}: {} points/{}", type, newPoints, currentRate.getUnit());
            }
        }
        log.info("Finished synchronizing metal market rates.");
    }
}
