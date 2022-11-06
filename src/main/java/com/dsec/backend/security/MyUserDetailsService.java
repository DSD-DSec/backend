package com.dsec.backend.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dsec.backend.model.UserModel;
import com.dsec.backend.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        List<UserModel> userModels = userRepository.findByEmailEquals(username);
        if (userModels == null || userModels.size() != 1)
            throw new UsernameNotFoundException("Bad credentials");

        return new UserPrincipal(userModels.get(0));
    }

}
