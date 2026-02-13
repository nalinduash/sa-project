package com.bookfair.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private String stallCode;
    private String stallSize;
    private String qrCode;
    private LocalDateTime reservationDate;
    private String businessName;
}