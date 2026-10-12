package com.transitops.repository;

import com.transitops.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByVehicleIdOrderByDateDesc(Long vehicleId);

    List<Expense> findByCategoryOrderByDateDesc(String category);

    List<Expense> findByVehicleIdAndCategoryOrderByDateDesc(Long vehicleId, String category);

    List<Expense> findAllByOrderByDateDesc();
}
