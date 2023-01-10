package com.dsec.backend.service.role;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<UserRole> getUserRoles() {
        return roleRepository.findAll();
    }

    @Override
    public UserRole getRole(int id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(UserRole.class, id));
    }

}
