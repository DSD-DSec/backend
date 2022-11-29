package com.dsec.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.dsec.backend.entity.UserEntity;

@Repository
public interface UserRepository
        extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    UserEntity findByEmail(String email);

    boolean existsByEmail(String email);

}
