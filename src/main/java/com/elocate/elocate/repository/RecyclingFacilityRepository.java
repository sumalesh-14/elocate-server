package com.elocate.elocate.repository;

import com.elocate.elocate.model.RecyclingFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecyclingFacilityRepository extends JpaRepository<RecyclingFacility, UUID> {
}