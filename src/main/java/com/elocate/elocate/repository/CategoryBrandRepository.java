package com.elocate.elocate.repository;

import com.elocate.elocate.model.CategoryBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryBrandRepository extends JpaRepository<CategoryBrand, UUID> {

    Page<CategoryBrand> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<CategoryBrand> findByBrandId(UUID brandId, Pageable pageable);

    List<CategoryBrand> findByCategoryId(UUID categoryId);

    List<CategoryBrand> findByBrandId(UUID brandId);

    Optional<CategoryBrand> findByCategoryIdAndBrandId(UUID categoryId, UUID brandId);

    boolean existsByCategoryIdAndBrandId(UUID categoryId, UUID brandId);
}
