package com.dsec.backend.service.tool;

import com.dsec.backend.entity.ToolEntity;

import java.util.List;

public interface ToolService {

    List<ToolEntity> getTools();

    ToolEntity getTool(long id);
}
