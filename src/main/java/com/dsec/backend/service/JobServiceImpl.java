package com.dsec.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.dsec.backend.entity.Job;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.repository.JobRepository;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepo;

    private final RepoRepository repoRepo;

    private final UserService userService;

    @Override
    public List<Job> getJobsByRepoID(long repoID, Jwt jwt) {

        Repo repo = repoRepo.findById(repoID)
                .orElseThrow(() -> new EntityMissingException(Repo.class, repoID));

        if (!isUserInRepo(repo, jwt))
            throw new ForbidenAccessException();

        return jobRepo.findAllByRepoId(repoID);
    }

    @Override
    public Job fetch(long jobID, Jwt jwt) {
        Job job = jobRepo.findById(jobID)
                .orElseThrow(() -> new EntityMissingException(Job.class, jobID));

        if (!isUserInRepo(job.getRepo(), jwt))
            throw new ForbidenAccessException();

        return job;
    }

    @Override
    public List<Job> findAll() {
        return jobRepo.findAll();
    }

    private boolean isUserInRepo(Repo repo, Jwt jwt) {
        long userId = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity().getId();

        return repo.getUsers().contains(userService.fetch(userId));
    }

}
