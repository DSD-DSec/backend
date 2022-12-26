package com.dsec.backend.service.tool;

import java.util.List;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.Tool;
import com.dsec.backend.entity.ToolEntity;

public interface ToolService {

    List<ToolEntity> getTools();

    ToolEntity getToolByID(int id);

    ToolEntity getToolByName(Tool tool);

    void priorityMatrix(Repo repo);

    void importData();
}
