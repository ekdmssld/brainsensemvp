package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
    private String username;
    private String email;
    private String password;
    private String passwordConfirm;
    private String phone;
    private String address;
}