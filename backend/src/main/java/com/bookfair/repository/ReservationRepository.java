package com.bookfair.repository;

import com.bookfair.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    long countByUserId(Long userId);
    boolean existsByStallId(Long stallId);
}