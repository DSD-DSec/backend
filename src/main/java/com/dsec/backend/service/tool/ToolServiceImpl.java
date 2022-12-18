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
}
