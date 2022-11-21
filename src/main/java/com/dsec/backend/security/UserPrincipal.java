package com.dsec.backend.security;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.dsec.backend.entity.Role;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRole;

public class UserPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = -6328324723198029932L;

    private final UserEntity userEntity;

    public UserPrincipal(UserEntity UserEntity) {
        this.userEntity = UserEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return mapToGrantedAuthorities(List.of(this.userEntity.getUserRole().getRoleName()));
    }

    private List<? extends GrantedAuthority> mapToGrantedAuthorities(List<Role> list) {
        return list.stream().map(r -> new SimpleGrantedAuthority(r.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("email", userEntity.getEmail());
        map.put("firstName", userEntity.getFirstName());
        map.put("lastName", userEntity.getLastName());
        map.put("id", userEntity.getId().toString());
        map.put("roleId", userEntity.getUserRole().getId().toString());
        map.put("roleName", userEntity.getUserRole().getRoleName().toString());

        return map;
    }

    public static UserPrincipal fromClaims(Map<String, Object> map) {
        UserRole userRole = new UserRole(Integer.valueOf((String) map.get("roleId")),
                Role.valueOf((String) map.get("roleName")));

        UserEntity userEntity = UserEntity.builder().id(Long.valueOf((String) map.get("id")))
                .firstName((String) map.get("firstName"))
                .lastName((String) map.get("lastName"))
                .email((String) map.get("email"))
                .password(null)
                .userRole(userRole).build();

        return new UserPrincipal(userEntity);
    }

}
