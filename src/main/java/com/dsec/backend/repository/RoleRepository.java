package com.dsec.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dsec.backend.model.Role;
import com.dsec.backend.model.UserRole;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, Integer> {

    UserRole getByRoleNameEquals(Role role);

}
