package com.bookingsystem.application.dto;

import com.bookingsystem.application.enums.Role;
import lombok.Data;

@Data
public class AuthResponse {
    public String token;
    public String username;
    public String email;
    public Role role;
}
