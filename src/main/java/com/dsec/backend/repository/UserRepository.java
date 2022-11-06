package com.dsec.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.dsec.backend.model.UserModel;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface UserRepository extends JpaRepository<UserModel, Integer> {

    List<UserModel> findByEmailEquals(String email);

}
