package com.bookfair.model;
//Dummy

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stall_id", nullable = false)
    private Stall stall;
    
    @Column(unique = true, nullable = false)
    private String qrCode;
    
    @Column(nullable = false)
    private LocalDateTime reservationDate;
    
    @Column(nullable = false)
    private String confirmationEmail;
    
    @PrePersist
    public void prePersist() {
        this.reservationDate = LocalDateTime.now();
    }
}