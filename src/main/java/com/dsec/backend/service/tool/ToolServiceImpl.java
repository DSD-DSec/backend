package com.dsec.backend.service.tool;

import com.dsec.backend.entity.*;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.repository.ToolRepoRepository;
import com.dsec.backend.repository.ToolRepository;
import com.dsec.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ToolServiceImpl implements  ToolService{

    private final ToolRepository toolRepository;

    private final ToolRepoRepository toolRepoRepository;

    private final RepoRepository repoRepository;

    @Override
    public List<ToolEntity> getTools() {
        return toolRepository.findAll();
    }

    @Override
    public ToolEntity getToolByID(int id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(ToolEntity.class, id));
    }

    @Override
    public ToolEntity getToolByName(Tool tool) {
        try{
            return toolRepository.findByToolName(tool);
        }
        catch (EntityMissingException e){
            throw new EntityMissingException(ToolEntity.class, tool);
        }
    }

    @Override
    public void priorityMatrix(Repo repo) {
        List<ToolEntity> suggestion = new ArrayList<>();
        List<ToolEntity> tools = getTools();

        int sufficiency = 9;

        for (ToolEntity tool : tools) {
            int score = 0;
            score += repo.getSecurity() * tool.getSecurity();
            score += repo.getPrivacy() * tool.getPrivacy();
            if (repo.getUserData()) {
                score += tool.getUserData();
            }
            if (!repo.getLanguage().equals(tool.getLanguage()) && !tool.getLanguage().equals(Language.NONE)) {
                score = 0;
            }
            if (score >= sufficiency) {
                suggestion.add(toolRepository.findByToolName(tool.getToolName()));
            }

        }

        for(ToolEntity toolEntity:suggestion) {
            toolRepoRepository.save(new ToolRepo(null, repo, toolEntity));
        }

    }

    @Override
    public void importData() {
        int tool_num = 5;
        // Tools are added to Tool Entity table
        if(toolRepository.count() != tool_num) {
            toolRepository.save(new ToolEntity(Tool.GITLEAKS, 5, 4, 5, Language.NONE));
            toolRepository.save(new ToolEntity(Tool.BANDIT, 1, 5, 2, Language.PYTHON));
            toolRepository.save(new ToolEntity(Tool.FLAWFINDER, 1, 5, 2, Language.C_CPP));
            toolRepository.save(new ToolEntity(Tool.GOKART, 1, 5, 2, Language.GO));
            toolRepository.save(new ToolEntity(Tool.PROGPILOT, 1, 5, 2, Language.PHP));
        }

    }

    @Override
    public List<ToolEntity> getToolsByRepo(long id, Jwt jwt) {
        Optional<Repo> repo = repoRepository.findById(id);

        if(repo.isEmpty())
            throw new EntityMissingException(Repo.class, id);

        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        if (!isOwner(repo.get(), userJwt))
            throw new ForbidenAccessException("Invalid repo get.");

        List<ToolEntity> toolEntities = new LinkedList<>();
        for(ToolRepo toolRepo : toolRepoRepository.getToolRepoByRepo(repo.get()))
            toolEntities.add(toolRepo.getTool());
        return toolEntities;
    }

    private boolean isOwner(Repo repo, UserEntity userJwt) {
        return repo.getUserRepos().stream().filter(UserRepo::getIsOwner)
                .allMatch(ur -> ur.getUser().getId().equals(userJwt.getId()));
    }
}
