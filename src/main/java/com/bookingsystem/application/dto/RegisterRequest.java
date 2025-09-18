package com.bookingsystem.application.dto;

import com.bookingsystem.application.enums.Country;
import lombok.Data;

@Data
public class RegisterRequest {
    public String username;
    public String email;
    public String password;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public Country country;
}
