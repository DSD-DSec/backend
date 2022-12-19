package com.dsec.backend.service.tool;

import com.dsec.backend.entity.*;

import java.util.ArrayList;
import java.util.List;

public interface ToolService {

    List<ToolEntity> getTools();

    ToolEntity getToolByID(int id);

    ToolEntity getToolByName(Tool tool);

    ArrayList<Tool> priorityMatrix(Repo repo);

    void importData();
}
