package com.bookfair.service;

//Dummy

import com.bookfair.dto.ReservationResponse;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReservationService {
    public List<ReservationResponse> getAllReservations() {
        return List.of();
    }
}