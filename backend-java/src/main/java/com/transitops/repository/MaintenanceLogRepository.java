package com.transitops.repository;

import com.transitops.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    List<MaintenanceLog> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<MaintenanceLog> findByStatusOrderByCreatedAtDesc(String status);

    List<MaintenanceLog> findByVehicleIdAndStatusOrderByCreatedAtDesc(Long vehicleId, String status);

    List<MaintenanceLog> findAllByOrderByCreatedAtDesc();
}
