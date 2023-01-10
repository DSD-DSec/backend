package com.dsec.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsec.backend.entity.Role;
import com.dsec.backend.entity.UserRole;

public interface RoleRepository extends JpaRepository<UserRole, Integer> {

    UserRole getByRoleNameEquals(Role role);

}
