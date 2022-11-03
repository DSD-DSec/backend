package com.dsec.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.dsec.backend.model.Role;
import com.dsec.backend.model.UserRole;

@RepositoryRestResource(collectionResourceRel = "userRoles", path = "userRoles")
public interface RoleRepository extends JpaRepository<UserRole, Integer> {

    UserRole getByRoleNameEquals(Role role);

}
