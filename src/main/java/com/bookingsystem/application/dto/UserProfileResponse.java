package com.bookingsystem.application.dto;

import com.bookingsystem.application.enums.Country;
import com.bookingsystem.application.enums.Role;
import com.bookingsystem.application.enums.Status;

public class UserProfileResponse {
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public Country country;
    public Role role;
    public Status status;
}

