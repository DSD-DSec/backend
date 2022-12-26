package com.dsec.backend.service.tool;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dsec.backend.entity.Language;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.Tool;
import com.dsec.backend.entity.ToolEntity;
import com.dsec.backend.entity.ToolRepo;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.repository.ToolRepoRepository;
import com.dsec.backend.repository.ToolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ToolServiceImpl implements ToolService {

    private final ToolRepository toolRepository;

    private final ToolRepoRepository toolRepoRepository;

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
        try {
            return toolRepository.findByToolName(tool);
        } catch (EntityMissingException e) {
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
                suggestion.add(tool);
            }

        }

        for (ToolEntity toolEntity : suggestion) {
            toolRepoRepository.save(new ToolRepo(null, repo, toolEntity));
        }

    }

    @Override
    public void importData() {
        int tool_num = 5;
        // Tools are added to Tool Entity table
        if (toolRepository.count() != tool_num) {
            toolRepository.save(new ToolEntity(Tool.GITLEAKS, 5, 4, 5, Language.NONE));
            toolRepository.save(new ToolEntity(Tool.BANDIT, 1, 5, 2, Language.PYTHON));
            toolRepository.save(new ToolEntity(Tool.FLAWFINDER, 1, 5, 2, Language.C_CPP));
            toolRepository.save(new ToolEntity(Tool.GOKART, 1, 5, 2, Language.GO));
            toolRepository.save(new ToolEntity(Tool.PROGPILOT, 1, 5, 2, Language.PHP));
        }

    }

}
