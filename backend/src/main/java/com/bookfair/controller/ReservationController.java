package com.bookfair.controller;

import com.bookfair.dto.*;
import com.bookfair.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<List<ReservationResponse>> createReservation(
            @RequestBody ReservationRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(reservationService.createReservations(email, request));
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(reservationService.getUserReservations(email));
    }
}