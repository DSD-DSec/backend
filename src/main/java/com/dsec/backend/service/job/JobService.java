package com.dsec.backend.service.job;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

import com.dsec.backend.entity.Job;
import com.dsec.backend.model.job.JobDTO;

public interface JobService {

    List<JobDTO<?>> getJobsByRepoID(long repoID, Jwt jwt);

    List<JobDTO<?>> findAll();

    Job fetch(long jobID, Jwt jwt);

    JobDTO<?> get(long jobID, Jwt jwt);
}
