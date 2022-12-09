package com.dsec.backend.service;

import com.dsec.backend.entity.Job;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.exception.UnauthorizedAccessException;
import com.dsec.backend.repository.JobRepository;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepo;

    private final RepoRepository repoRepo;

    @Override
    public List<Job> getJobsByRepoID(long repoID, Jwt jwt) {

        Repo repo = repoRepo.findById(repoID)
                .orElseThrow(() -> new EntityMissingException(Repo.class, repoID));


        if(!isUserInRepo(repo, UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity()))
            throw new UnauthorizedAccessException();

        return jobRepo.findAllByRepo_Id(repoID);
    }

    @Override
    public Job fetch(long jobID, Jwt jwt)
    {
        Job job =  jobRepo.findById(jobID)
                .orElseThrow(() -> new EntityMissingException(Job.class, jobID));

        if(!isUserInRepo(job.getRepo(), UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity()))
            throw new UnauthorizedAccessException();

        return job;
    }

    @Override
    public List<Job> findAll()
    {
        return jobRepo.findAll();
    }

    private boolean isUserInRepo(Repo repo, UserEntity user)
    {
        for(UserEntity userEntity : repo.getUsers())
            if(userEntity.getId().equals(user.getId()))
                return true;

        return false;
    }

}
