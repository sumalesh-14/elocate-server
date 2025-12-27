package com.elocate.elocate.service;

import com.elocate.elocate.model.DeviceModel;
import com.elocate.elocate.model.MetalRate;
import com.elocate.elocate.model.MetalType;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.RewardSnapshot;
import com.elocate.elocate.repository.RewardSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing reward snapshots
 * Snapshots freeze the calculation at request creation time
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RewardSnapshotService {
    
    private final RewardSnapshotRepository snapshotRepository;
    
    /**
     * Create and save reward snapshot
     */
    @Transactional
    public RewardSnapshot createSnapshot(
            RecycleRequest recycleRequest,
            DeviceModel deviceModel,
            Map<MetalType, MetalRate> metalRates,
            BigDecimal conditionMultiplier,
            BigDecimal totalPoints) {
        
        log.info("Creating reward snapshot for recycle request: {}", recycleRequest.getId());
        
        RewardSnapshot snapshot = RewardSnapshot.builder()
                .recycleRequest(recycleRequest)
                .goldMg(deviceModel.getGoldMg())
                .silverMg(deviceModel.getSilverMg())
                .copperG(deviceModel.getCopperG())
                .palladiumMg(deviceModel.getPalladiumMg())
                .conditionMultiplier(conditionMultiplier)
                .totalPoints(totalPoints)
                .build();
        
        RewardSnapshot saved = snapshotRepository.save(snapshot);
        log.info("Reward snapshot created with id: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Get snapshot by recycle request ID
     */
    @Transactional(readOnly = true)
    public Optional<RewardSnapshot> getSnapshotByRequestId(UUID recycleRequestId) {
        log.info("Fetching reward snapshot for recycle request: {}", recycleRequestId);
        return snapshotRepository.findByRecycleRequestId(recycleRequestId);
    }
}
