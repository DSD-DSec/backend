package com.dsec.backend.service.tool;

import com.dsec.backend.entity.ToolEntity;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public ToolEntity getTool(long id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(UserRole.class, id));
    }
}
