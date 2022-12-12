package com.dsec.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsec.backend.entity.UserRepo;

public interface UserRepoRepository extends JpaRepository<UserRepo, Long> {
    
}
