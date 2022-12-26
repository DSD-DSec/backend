package com.dsec.backend.service.job;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.dsec.backend.entity.Job;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.Tool;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.model.job.JobDTO;
import com.dsec.backend.model.tools.BanditDTO;
import com.dsec.backend.model.tools.GitleaksDTO;
import com.dsec.backend.model.tools.GoKartDTO;
import com.dsec.backend.model.tools.ProgPilotDTO;
import com.dsec.backend.model.tools.flawfinder.Sarif210Rtm5;
import com.dsec.backend.repository.JobRepository;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.security.UserPrincipal;
import com.dsec.backend.util.attrconverter.LogConverter;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepo;
    private final RepoRepository repoRepo;

    private final Map<Tool, Function<Job, JobDTO<?>>> mapOfFunctions;

    public JobServiceImpl(JobRepository jobRepo, RepoRepository repoRepo, LogConverter logConverter) {
        this.jobRepo = jobRepo;
        this.repoRepo = repoRepo;

        this.mapOfFunctions = new EnumMap<>(Tool.class);
        mapOfFunctions.put(Tool.BANDIT, j -> {
            JobDTO<BanditDTO> dto = new JobDTO<>();
            copyProps(j, dto);
            dto.setLog(logConverter.convertToEntityAttribute(j.getLog(), BanditDTO.class));
            return dto;
        });
        mapOfFunctions.put(Tool.GITLEAKS, j -> {
            JobDTO<GitleaksDTO> dto = new JobDTO<>();
            copyProps(j, dto);
            dto.setLog(logConverter.convertToEntityAttribute(j.getLog(), GitleaksDTO.class));
            return dto;
        });
        mapOfFunctions.put(Tool.GOKART, j -> {
            JobDTO<GoKartDTO> dto = new JobDTO<>();
            copyProps(j, dto);
            dto.setLog(logConverter.convertToEntityAttribute(j.getLog(), GoKartDTO.class));
            return dto;
        });
        mapOfFunctions.put(Tool.PROGPILOT, j -> {
            JobDTO<ProgPilotDTO> dto = new JobDTO<>();
            copyProps(j, dto);
            dto.setLog(logConverter.convertToEntityAttribute(j.getLog(), ProgPilotDTO.class));
            return dto;
        });
        mapOfFunctions.put(Tool.FLAWFINDER, j -> {
            JobDTO<Sarif210Rtm5> dto = new JobDTO<>();
            copyProps(j, dto);
            dto.setLog(logConverter.convertToEntityAttribute(j.getLog(), Sarif210Rtm5.class));
            return dto;
        });
    }

    @Override
    public List<JobDTO<?>> getJobsByRepoID(long repoID, Jwt jwt) {

        Repo repo = repoRepo.findById(repoID)
                .orElseThrow(() -> new EntityMissingException(Repo.class, repoID));

        if (!isUserInRepo(repo, jwt))
            throw new ForbidenAccessException();

        return jobRepo.findAllByRepoId(repoID).stream().map(j -> mapOfFunctions.get(j.getTool().getToolName()).apply(j))
                .collect(Collectors.toList());
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
    public List<JobDTO<?>> findAll() {
        return jobRepo.findAll().stream().map(j -> mapOfFunctions.get(j.getTool().getToolName()).apply(j))
                .collect(Collectors.toList());
    }

    @Override
    public JobDTO<?> get(long jobID, Jwt jwt) {
        Job job = fetch(jobID, jwt);
        return mapOfFunctions.get(job.getTool().getToolName()).apply(job);
    }

    private boolean isUserInRepo(Repo repo, Jwt jwt) {
        long userId = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity().getId();

        return repo.getUserRepos().stream().anyMatch(o -> o.getUser().getId().equals(userId));
    }

    private void copyProps(Job j, JobDTO<?> dto) {
        dto.setCompliant(j.getCompliant());
        dto.setEndTime(j.getEndTime());
        dto.setId(j.getId());
        dto.setRepo(j.getRepo());
        dto.setStartTime(j.getStartTime());
        dto.setTool(j.getTool());
    }

}
