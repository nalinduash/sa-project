package com.bookfair.dto;

import lombok.*;

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