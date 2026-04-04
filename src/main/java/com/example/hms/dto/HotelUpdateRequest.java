package com.example.hms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelUpdateRequest {

    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
}
