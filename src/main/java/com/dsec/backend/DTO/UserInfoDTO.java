package com.dsec.backend.DTO;

import com.dsec.backend.model.UserRole;

public record UserInfoDTO(Integer id, String username, String firstName, String lastName,
        UserRole userRole) {

}
