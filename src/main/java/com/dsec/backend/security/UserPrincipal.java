package com.dsec.backend.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dsec.backend.model.Role;
import com.dsec.backend.model.UserModel;

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
        return list.stream().map(r -> new SimpleGrantedAuthority(r.toString())).collect(Collectors.toList());
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

}
