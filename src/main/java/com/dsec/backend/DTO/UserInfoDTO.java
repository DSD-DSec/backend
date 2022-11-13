package com.dsec.backend.DTO;

import com.dsec.backend.model.UserRole;

public record UserInfoDTO(Integer id, String email, String firstName, String lastName,
        UserRole userRole) {

}