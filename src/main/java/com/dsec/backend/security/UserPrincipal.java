package com.dsec.backend.security;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dsec.backend.model.Role;
import com.dsec.backend.model.UserModel;
import com.dsec.backend.model.UserRole;

public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = -6328324723198029932L;

    private final UserModel userModel;

    public UserPrincipal(UserModel UserModel) {
        this.userModel = UserModel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return mapToGrantedAuthorities(List.of(this.userModel.getUserRole().getRoleName()));
    }

    private List<? extends GrantedAuthority> mapToGrantedAuthorities(List<Role> list) {
        return list.stream().map(r -> new SimpleGrantedAuthority(r.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return userModel.getPassword();
    }

    @Override
    public String getUsername() {
        return userModel.getEmail();
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

    public UserModel getUserModel() {
        return userModel;
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("email", userModel.getEmail());
        map.put("firstName", userModel.getFirstName());
        map.put("lastName", userModel.getLastName());
        map.put("id", userModel.getId().toString());
        map.put("roleId", userModel.getUserRole().getId().toString());
        map.put("roleName", userModel.getUserRole().getRoleName().toString());

        return map;
    }

    public UserPrincipal fromClaims(Map<String, Object> map) {
        UserRole userRole = new UserRole(Integer.valueOf((String) map.get("roleId")),
                Role.valueOf((String) map.get("roleName")));

        UserModel userModel = new UserModel(Integer.valueOf((String) map.get("id")),
                (String) map.get("firstName"), (String) map.get("lastName"),
                (String) map.get("email"), null, userRole);

        return new UserPrincipal(userModel);
    }

}
