package com.bookfair.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StallDTO {
    private Long id;
    private String stallCode;
    private String size;
    private String location;
    private Double price;
    private Boolean isAvailable;
    private Integer rowPosition;
    private Integer columnPosition;
}