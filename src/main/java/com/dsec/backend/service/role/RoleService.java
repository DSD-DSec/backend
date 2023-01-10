package com.dsec.backend.service.role;

import java.util.List;
import com.dsec.backend.entity.UserRole;

public interface RoleService {

    List<UserRole> getUserRoles();

    UserRole getRole(int id);

}
