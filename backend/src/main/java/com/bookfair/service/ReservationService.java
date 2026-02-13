package com.bookfair.service;

import com.bookfair.dto.ReservationRequest;
import com.bookfair.dto.ReservationResponse;
import com.bookfair.model.Reservation;
import com.bookfair.model.Stall;
import com.bookfair.model.User;
import com.bookfair.repository.ReservationRepository;
import com.bookfair.repository.StallRepository;
import com.bookfair.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private StallRepository stallRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    private static final int MAX_STALLS_PER_USER = 3;
    
    @Transactional
    public List<ReservationResponse> createReservations(String email, ReservationRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        long existingReservations = reservationRepository.countByUserId(user.getId());
        if (existingReservations + request.getStallIds().size() > MAX_STALLS_PER_USER) {
            throw new RuntimeException("Maximum " + MAX_STALLS_PER_USER + " stalls allowed per business");
        }
        
        return request.getStallIds().stream()
            .map(stallId -> createSingleReservation(user, stallId))
            .collect(Collectors.toList());
    }
    
    private ReservationResponse createSingleReservation(User user, Long stallId) {
        Stall stall = stallRepository.findById(stallId)
            .orElseThrow(() -> new RuntimeException("Stall not found"));
        
        if (!stall.getIsAvailable()) {
            throw new RuntimeException("Stall " + stall.getStallCode() + " is already reserved");
        }
        
        String qrCode = generateQRCode(user.getBusinessName(), stall.getStallCode());
        
        Reservation reservation = Reservation.builder()
            .user(user)
            .stall(stall)
            .qrCode(qrCode)
            .confirmationEmail(user.getEmail())
            .build();
        
        stall.setIsAvailable(false);
        stallRepository.save(stall);
        reservationRepository.save(reservation);
        
        emailService.sendReservationConfirmation(user.getEmail(), user.getBusinessName(), 
            stall.getStallCode(), stall.getSize().name(), qrCode);
        
        return mapToResponse(reservation);
    }
    
    private String generateQRCode(String businessName, String stallCode) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            String data = String.format("BOOKFAIR:%s:%s:%d", businessName, stallCode, System.currentTimeMillis());
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
    
    public List<ReservationResponse> getUserReservations(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return reservationRepository.findByUserId(user.getId())
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
            .id(reservation.getId())
            .stallCode(reservation.getStall().getStallCode())
            .stallSize(reservation.getStall().getSize().name())
            .qrCode(reservation.getQrCode())
            .reservationDate(reservation.getReservationDate())
            .businessName(reservation.getUser().getBusinessName())
            .build();
    }
}