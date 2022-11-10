package com.dsec.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dsec.backend.model.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {

    List<UserModel> findByEmailEquals(String email);

}
