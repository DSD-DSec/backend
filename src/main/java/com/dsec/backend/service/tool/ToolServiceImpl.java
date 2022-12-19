package com.dsec.backend.service.tool;

import com.dsec.backend.entity.*;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ToolServiceImpl implements  ToolService{

    private final ToolRepository toolRepository;

    @Override
    public List<ToolEntity> getTools() {
        return toolRepository.findAll();
    }

    @Override
    public ToolEntity getTool(int id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(UserRole.class, id));
    }

    @Override
    public ArrayList<Integer> priorityMatrix(RepoType type, RepoDomain domain, Integer security, Integer privacy, Boolean userData, Language language) {
        ArrayList<Integer> suggestion = new ArrayList<>();
        List<ToolEntity> tools = getTools();

        int sufficiency = 9;

        for (ToolEntity tool : tools) {
            int score = 0;
            score += security * tool.getSecurity();
            score += privacy * tool.getPrivacy();
            if (userData) {
                score += tool.getUserData();
            }
            if (!language.equals(tool.getLanguage())) {
                score = 0;
            }
            if (score >= sufficiency) {
                suggestion.add(tool.getId());
            }

        }

        return suggestion;
    }

    @Override
    public void importData() {
        // Tool repository is cleaned
        toolRepository.deleteAll();

        // Tools are added to Tool Entity table
        toolRepository.save(new ToolEntity(Tool.GITLEAKS, 5, 4, 5, Language.NONE));
        toolRepository.save(new ToolEntity(Tool.BANDIT, 1, 5, 2, Language.PYTHON));
        toolRepository.save(new ToolEntity(Tool.FLAWFINDER, 1, 5, 2, Language.C_CPP));
        toolRepository.save(new ToolEntity(Tool.GOKART, 1, 5, 2, Language.GO));
        toolRepository.save(new ToolEntity(Tool.PROGPILOT, 1, 5, 2, Language.PHP));

    }
}
