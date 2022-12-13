package com.dsec.backend.service;

import com.dsec.backend.entity.Job;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface JobService {

    List<Job> getJobsByRepoID(long repoID, Jwt jwt);

    List<Job> findAll();

    Job fetch(long jobID, Jwt jwt);

}
