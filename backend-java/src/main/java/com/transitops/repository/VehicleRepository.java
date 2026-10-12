package com.transitops.repository;

import com.transitops.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Vehicle> findByStatusOrderByName(String status);

    @Modifying
    @Query("UPDATE Vehicle v SET v.odometerKm = v.odometerKm + :km, v.updatedAt = CURRENT_TIMESTAMP WHERE v.id = :id")
    void incrementOdometer(@Param("id") Long id, @Param("km") BigDecimal km);
}
