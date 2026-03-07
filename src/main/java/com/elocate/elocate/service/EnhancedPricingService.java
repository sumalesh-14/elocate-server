package com.elocate.elocate.service;

import com.elocate.elocate.dto.AIPricingRequest;
import com.elocate.elocate.dto.AIPricingResponse;
import com.elocate.elocate.dto.PricingBreakdownResponse;
import com.elocate.elocate.model.DeviceConditionFactor;
import com.elocate.elocate.model.DeviceModel;
import com.elocate.elocate.model.MetalRate;
import com.elocate.elocate.model.enums.MetalType;
import com.elocate.elocate.repository.DeviceConditionFactorRepository;
import com.elocate.elocate.repository.MetalRateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enhanced pricing service with AI integration and comprehensive pricing breakdown
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnhancedPricingService {

    private final AIPricingService aiPricingService;
    private final MetalRateRepository metalRateRepository;
    private final DeviceConditionFactorRepository conditionFactorRepository;
    private final ObjectMapper objectMapper;

    @Value("${wallet.points.to.money.rate:0.01}")
    private BigDecimal defaultPointsToMoneyRate;

    @Value("${wallet.currency.code:USD}")
    private String defaultCurrencyCode;

    /**
     * Calculate comprehensive pricing breakdown for a device
     * Tries AI pricing first, falls back to database pricing
     */
    public PricingBreakdownResponse calculatePricingBreakdown(
            DeviceModel deviceModel,
            String conditionCode,
            LocalDate requestDate) {

        log.info("Calculating pricing breakdown for model: {}, condition: {}", 
                 deviceModel.getModelName(), conditionCode);

        // Get condition factor
        DeviceConditionFactor conditionFactor = conditionFactorRepository
                .findByConditionCodeAndIsActive(conditionCode, true)
                .orElseThrow(() -> new IllegalArgumentException("Invalid condition code: " + conditionCode));

        // Determine base price source
        BigDecimal basePrice;
        String pricingSource;

        // Check for manual override first
        if (deviceModel.getManualPriceOverride() != null) {
            basePrice = deviceModel.getManualPriceOverride();
            pricingSource = "MANUAL_OVERRIDE";
            log.info("Using manual price override: {}", basePrice);
        }
        // Try AI pricing if available
        else if (aiPricingService.isAIPricingAvailable()) {
            Optional<AIPricingResponse> aiPricing = getAIPricingForModel(deviceModel, conditionCode);
            if (aiPricing.isPresent() && aiPricing.get().getEstimatedPrice() != null) {
                basePrice = aiPricing.get().getEstimatedPrice();
                pricingSource = "AI";
                log.info("Using AI pricing: {}", basePrice);
            } else {
                basePrice = calculateDatabaseBasePrice(deviceModel, requestDate);
                pricingSource = "DATABASE";
                log.info("AI pricing unavailable, using database pricing: {}", basePrice);
            }
        }
        // Fall back to database pricing
        else {
            basePrice = calculateDatabaseBasePrice(deviceModel, requestDate);
            pricingSource = "DATABASE";
            log.info("Using database pricing: {}", basePrice);
        }

        // Calculate materials value
        List<PricingBreakdownResponse.MaterialValueBreakdown> materialBreakdown = 
                calculateMaterialsBreakdown(deviceModel.getRecycleItems(), requestDate);
        
        BigDecimal materialsValue = materialBreakdown.stream()
                .map(PricingBreakdownResponse.MaterialValueBreakdown::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply condition multiplier
        BigDecimal conditionAdjustedPrice = basePrice.multiply(conditionFactor.getMultiplier())
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate total
        BigDecimal totalEstimatedPoints = conditionAdjustedPrice.add(materialsValue)
                .setScale(2, RoundingMode.HALF_UP);

        // Convert to monetary amount
        BigDecimal monetaryAmount = convertPointsToMoney(totalEstimatedPoints);

        return PricingBreakdownResponse.builder()
                .basePrice(basePrice)
                .conditionMultiplier(conditionFactor.getMultiplier())
                .conditionAdjustedPrice(conditionAdjustedPrice)
                .materialsValue(materialsValue)
                .totalEstimatedPoints(totalEstimatedPoints)
                .monetaryAmount(monetaryAmount)
                .currencyCode(defaultCurrencyCode)
                .materialBreakdown(materialBreakdown)
                .pricingSource(pricingSource)
                .build();
    }

    /**
     * Get AI pricing for a device model
     */
    private Optional<AIPricingResponse> getAIPricingForModel(DeviceModel model, String conditionCode) {
        AIPricingRequest request = AIPricingRequest.builder()
                .deviceModelName(model.getModelName())
                .categoryName(model.getCategory().getName())
                .brandName(model.getBrand().getName())
                .conditionCode(conditionCode)
                .build();

        return aiPricingService.getAIPricing(request);
    }

    /**
     * Calculate base price from database (precious materials)
     */
    private BigDecimal calculateDatabaseBasePrice(DeviceModel model, LocalDate requestDate) {
        List<PricingBreakdownResponse.MaterialValueBreakdown> breakdown = 
                calculateMaterialsBreakdown(model.getRecycleItems(), requestDate);
        
        return breakdown.stream()
                .map(PricingBreakdownResponse.MaterialValueBreakdown::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate materials value breakdown from recycle_items JSON
     */
    private List<PricingBreakdownResponse.MaterialValueBreakdown> calculateMaterialsBreakdown(
            String recycleItemsJson, LocalDate requestDate) {
        
        List<PricingBreakdownResponse.MaterialValueBreakdown> breakdown = new ArrayList<>();

        if (recycleItemsJson == null || recycleItemsJson.isBlank()) {
            return breakdown;
        }

        try {
            JsonNode root = objectMapper.readTree(recycleItemsJson);
            JsonNode materials = root.get("preciousMaterials");

            if (materials == null || !materials.isArray()) {
                return breakdown;
            }

            // Get active metal rates for the request date
            Map<MetalType, MetalRate> metalRates = getMetalRatesForDate(requestDate);

            for (JsonNode material : materials) {
                String typeStr = material.get("type").asText();
                BigDecimal quantity = new BigDecimal(material.get("quantity").asText());
                String unit = material.get("unit").asText();

                try {
                    MetalType metalType = MetalType.valueOf(typeStr);
                    MetalRate rate = metalRates.get(metalType);

                    if (rate != null) {
                        BigDecimal totalValue = quantity.multiply(rate.getPointsPerUnit())
                                .setScale(2, RoundingMode.HALF_UP);

                        breakdown.add(PricingBreakdownResponse.MaterialValueBreakdown.builder()
                                .metalType(typeStr)
                                .quantity(quantity)
                                .unit(unit)
                                .ratePerUnit(rate.getPointsPerUnit())
                                .totalValue(totalValue)
                                .build());
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown metal type: {}", typeStr);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing recycle items JSON", e);
        }

        return breakdown;
    }

    /**
     * Get metal rates active on a specific date
     */
    private Map<MetalType, MetalRate> getMetalRatesForDate(LocalDate date) {
        List<MetalRate> rates = metalRateRepository.findActiveRatesForDate(date);
        return rates.stream()
                .collect(Collectors.toMap(MetalRate::getMetalType, rate -> rate));
    }

    /**
     * Convert points to monetary amount
     */
    public BigDecimal convertPointsToMoney(BigDecimal points) {
        return points.multiply(defaultPointsToMoneyRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convert monetary amount to points
     */
    public BigDecimal convertMoneyToPoints(BigDecimal money) {
        return money.divide(defaultPointsToMoneyRate, 2, RoundingMode.HALF_UP);
    }
}
