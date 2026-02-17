package com.bookfair.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String email;
    private String password;
    private String businessName;
    private String contactPerson;
    private String phone;
    private String address;
}