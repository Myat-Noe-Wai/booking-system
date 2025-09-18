package com.bookingsystem.application.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    public String email;
    public String newPassword;
}
