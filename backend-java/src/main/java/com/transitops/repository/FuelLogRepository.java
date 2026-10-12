package com.transitops.repository;

import com.transitops.entity.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelLogRepository extends JpaRepository<FuelLog, Long> {

    List<FuelLog> findByVehicleIdOrderByDateDescCreatedAtDesc(Long vehicleId);

    List<FuelLog> findByTripIdOrderByDateDesc(Long tripId);

    List<FuelLog> findAllByOrderByDateDescCreatedAtDesc();
}
