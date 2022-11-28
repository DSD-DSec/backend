package com.dsec.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsec.backend.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>  {
    
}