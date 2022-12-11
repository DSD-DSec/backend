package com.dsec.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsec.backend.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long>  {

    List<Job> findAllByRepoId(long id);


}
